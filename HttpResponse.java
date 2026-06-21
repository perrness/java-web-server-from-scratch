record HttpResponse(int statusCode, String statusText, String contentType, String body) {
    private static HttpResponse text(int code, String status, String body) {
        return new HttpResponse(code, status, "text/plain", body);
    }

    static HttpResponse ok(String body) {
        return text(200, "OK", body);
    }

    static HttpResponse noContent() {
        return text(204, "No Content", "No Content");
    }

    static HttpResponse notFound() {
        return text(404, "Not Found", "Not Found");
    }

    static HttpResponse methodNotAllowed() {
        return text(405, "Method Not Allowed", "Method Not Allowed");
    }

    static HttpResponse html(String body) {
        return new HttpResponse(200, "OK", "text/html", body);
    }
}
