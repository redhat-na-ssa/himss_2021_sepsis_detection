
package com.redhat.naps.process.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Root Type for SepsisResponse
 * <p>
 * Results from analyzing patient vitals information
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "issepsis"
})
public class SepsisResponse {

    @JsonProperty("issepsis")
    private Integer issepsis;

    @JsonProperty("issepsis")
    public Integer getIssepsis() {
        return issepsis;
    }

    @JsonProperty("issepsis")
    public void setIssepsis(Integer issepsis) {
        this.issepsis = issepsis;
    }

}
