record HttpResponse(int statusCode, String statusText, String contentType, String body) {
    static HttpResponse ok(String body) {
        return new HttpResponse(200, "OK", "text/plain", body);
    }

    static HttpResponse noContent() {
        return new HttpResponse(204, "No Content", "text/plain", "No Content");
    }

    static HttpResponse html(String body) {
        return new HttpResponse(200, "OK", "text/html", body);
    }

    static HttpResponse notFound() {
        return new HttpResponse(404, "Not Found", "text/plain", "Not Found");
    }

    static HttpResponse methodNotAllowed() {
        return new HttpResponse(405, "Method Not Allowed", "text/plain", "Method Not Allowed");
    }
}
