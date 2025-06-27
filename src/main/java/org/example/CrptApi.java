package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kevinsawicki.http.HttpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CrptApi {
    private final BlockingQueue<Runnable> queue;
    public static final TimeUnit timeUnit = TimeUnit.SECONDS;
    public static final int requestLimit = 10;
    public static final int corePoolSize = 5;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        queue = new ArrayBlockingQueue<>(requestLimit, true);
        final long delayMilliseconds = TimeUnit.MILLISECONDS.convert(1, timeUnit) / requestLimit;
        Executors.newScheduledThreadPool(corePoolSize).scheduleWithFixedDelay(
                new TaskRunner(), 0L, delayMilliseconds, TimeUnit.MILLISECONDS);
    }

    private void createDocument(Object document, String signature) throws InterruptedException {
        queue.put(new DocumentProducer(document, signature));
    }

    public static class DocumentProducer implements Runnable {
        final Object document;
        final String signature;

        public DocumentProducer(Object document, String signature) {
            this.document = document;
            this.signature = signature;
        }

        public void run() {
            ObjectNode documentAsJSON = createJSONDocument();
            String url = "https://ismp.crpt.ru/api/v3/lk/documents/create";
            Map<String, String> parameters = new HashMap<>();
            parameters.put("pg", "product_group");
            String urlWithParameters = HttpRequest.append(url, parameters);
            HttpRequest request = HttpRequest.post(urlWithParameters);;
            request.header("Content-Type", "application/json");
            String content = documentAsJSON.toString();
            if (content != null && !content.isBlank()) {
                request.send(content);
                String response = request.body();
                int responseCode = request.code();
                if (responseCode >= 200 && responseCode < 300) {
                    System.out.println("Document creation response: " + response);
                } else {
                    System.out.println("Exception while processing request. Response code: " + responseCode);
                }
            }
        }

        private ObjectNode createJSONDocument() {
            ObjectMapper objectMapper = new ObjectMapper();
            final int productGroup = 1;
            String productDocument;
            ObjectNode documentAsJSON = objectMapper.createObjectNode();;
            try {
                productDocument = objectMapper.writeValueAsString(document);
                JsonNode productDocumentNode = objectMapper.readTree(productDocument);
                documentAsJSON.put("document_format", "MANUAL");
                documentAsJSON.set("product_document", productDocumentNode);
                documentAsJSON.put("product_group", productGroup);
                documentAsJSON.put("signature", signature);
                documentAsJSON.put("type", "LP_INTRODUCE_GOODS");
            } catch (JsonProcessingException e) {
                System.out.println("Exception while processing json: " + e.getMessage());
                throw new RuntimeException(e);
            }
            return documentAsJSON;
        }
    }

    public class TaskRunner implements Runnable {
        @Override
        public void run() {
            try {
                queue.take().run();
            } catch (InterruptedException e) {
                System.out.println("Document creation was interrupted: " + e.getMessage());
            }
        }
    }

    public static class Description {
        private String participantInn;

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }
    }

    public static class Product {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

        public String getCertificate_document() {
            return certificate_document;
        }

        public void setCertificate_document(String certificate_document) {
            this.certificate_document = certificate_document;
        }

        public String getCertificate_document_date() {
            return certificate_document_date;
        }

        public void setCertificate_document_date(String certificate_document_date) {
            this.certificate_document_date = certificate_document_date;
        }

        public String getCertificate_document_number() {
            return certificate_document_number;
        }

        public void setCertificate_document_number(String certificate_document_number) {
            this.certificate_document_number = certificate_document_number;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }

        public String getProduction_date() {
            return production_date;
        }

        public void setProduction_date(String production_date) {
            this.production_date = production_date;
        }

        public String getTnved_code() {
            return tnved_code;
        }

        public void setTnved_code(String tnved_code) {
            this.tnved_code = tnved_code;
        }

        public String getUit_code() {
            return uit_code;
        }

        public void setUit_code(String uit_code) {
            this.uit_code = uit_code;
        }

        public String getUitu_code() {
            return uitu_code;
        }

        public void setUitu_code(String uitu_code) {
            this.uitu_code = uitu_code;
        }
    }

    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private ArrayList<Product> products;
        private String reg_date;
        private String reg_number;

        public Description getDescription() {
            return description;
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public String getDoc_id() {
            return doc_id;
        }

        public void setDoc_id(String doc_id) {
            this.doc_id = doc_id;
        }

        public String getDoc_status() {
            return doc_status;
        }

        public void setDoc_status(String doc_status) {
            this.doc_status = doc_status;
        }

        public String getDoc_type() {
            return doc_type;
        }

        public void setDoc_type(String doc_type) {
            this.doc_type = doc_type;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }

        public String getParticipant_inn() {
            return participant_inn;
        }

        public void setParticipant_inn(String participant_inn) {
            this.participant_inn = participant_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }

        public String getProduction_date() {
            return production_date;
        }

        public void setProduction_date(String production_date) {
            this.production_date = production_date;
        }

        public String getProduction_type() {
            return production_type;
        }

        public void setProduction_type(String production_type) {
            this.production_type = production_type;
        }

        public ArrayList<Product> getProducts() {
            return products;
        }

        public void setProducts(ArrayList<Product> products) {
            this.products = products;
        }

        public String getReg_date() {
            return reg_date;
        }

        public void setReg_date(String reg_date) {
            this.reg_date = reg_date;
        }

        public String getReg_number() {
            return reg_number;
        }

        public void setReg_number(String reg_number) {
            this.reg_number = reg_number;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        CrptApi crptApi = new CrptApi(timeUnit, requestLimit);
        Object document = new Document();
        String signature = "signature";
        crptApi.createDocument(document, signature);
    }
}
