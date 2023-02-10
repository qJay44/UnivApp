package edu.muiv.univapp.api

enum class StatusCode(val code: Int) : StatusCodeMessage {
    /**
     * Response codes ->
     * 200: Response is OK
     * 204: Response is OK but no content
     * 500: Unexpected fail
     * 503: Service is unavailable
     */

    OK(200) {
        override fun message(objName: String): String = "OK"
    },

    NO_CONTENT(204) {
        override fun message(objName: String): String {
            return String.format(MSG_TEMPLATE, objName, "Haven't got any $objName", code)
        }
    },

    INTERNAL_SERVER_ERROR(500) {
        override fun message(objName: String): String {
            return String.format(MSG_TEMPLATE, objName, "Got unexpected fail", code)
        }
   },
    SERVICE_UNAVAILABLE(503) {
        override fun message(objName: String): String {
            return String.format(MSG_TEMPLATE, objName, "Server isn't working", code)
        }
    };

    companion object {
        private const val MSG_TEMPLATE = "Fetched %s: %s (%d)"
    }
}
