package fi.nls.dbquality.model;

public class BadQueryQualityResult extends QualityResult {
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
