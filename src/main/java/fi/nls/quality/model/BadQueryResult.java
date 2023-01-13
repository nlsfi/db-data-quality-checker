/*
 * TODO Add license and copyright.
 */
package fi.nls.quality.model;

public class BadQueryResult extends QualityQueryResult {

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
