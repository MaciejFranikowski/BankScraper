package com.kontomatik.bankScraper.mbank.models;

import java.util.HashMap;
import java.util.Map;

public record RequestParams(
        Map<String, String> cookies,
        Map<String, String> headers,
        Map<String, String> data,
        boolean ignoreContentType,
        String requestBody
) {
    public static class Builder {
        private Map<String, String> cookies = new HashMap<>();
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> data = new HashMap<>();
        private boolean ignoreContentType;
        private String requestBody;

        public Builder cookies(Map<String, String> cookies) {
            this.cookies = cookies;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder data(Map<String, String> data) {
            this.data = data;
            return this;
        }

        public Builder ignoreContentType(boolean ignoreContentType) {
            this.ignoreContentType = ignoreContentType;
            return this;
        }

        public Builder requestBody(String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public RequestParams build() {
            return new RequestParams(cookies, headers, data, ignoreContentType, requestBody);
        }
    }
}
