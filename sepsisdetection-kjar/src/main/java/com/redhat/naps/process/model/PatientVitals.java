
package com.redhat.naps.process.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Root Type for PatientVitals
 * <p>
 * Contains a set of data collected from a patient by their caregiver.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "HR",
    "O2Sat",
    "Temp",
    "SBP",
    "MAP",
    "DBP",
    "Resp",
    "EtCO2",
    "BaseExcess",
    "HCO3",
    "FiO2",
    "pH",
    "PaCO2",
    "SaO2",
    "AST",
    "BUN",
    "Alkalinephos",
    "Calcium",
    "Chloride",
    "Creatinine",
    "Glucose",
    "Lactate",
    "Magnesium",
    "Phosphate",
    "Potassium",
    "Bilirubin_total",
    "Hct",
    "Hgb",
    "PTT",
    "WBC",
    "Fibrinogen",
    "Platelets"
})
public class PatientVitals implements Serializable{

    @JsonProperty("HR")
    private Double hr;
    @JsonProperty("O2Sat")
    private Double o2Sat;
    @JsonProperty("Temp")
    private Double temp;
    @JsonProperty("SBP")
    private Double sbp;
    @JsonProperty("MAP")
    private Double map;
    @JsonProperty("DBP")
    private Double dbp;
    @JsonProperty("Resp")
    private Double resp;
    @JsonProperty("EtCO2")
    private Double etCO2;
    @JsonProperty("BaseExcess")
    private Double baseExcess;
    @JsonProperty("HCO3")
    private Double hco3;
    @JsonProperty("FiO2")
    private Double fiO2;
    @JsonProperty("pH")
    private Double pH;
    @JsonProperty("PaCO2")
    private Double paCO2;
    @JsonProperty("SaO2")
    private Double saO2;
    @JsonProperty("AST")
    private Double ast;
    @JsonProperty("BUN")
    private Double bun;
    @JsonProperty("Alkalinephos")
    private Double alkalinephos;
    @JsonProperty("Calcium")
    private Double calcium;
    @JsonProperty("Chloride")
    private Double chloride;
    @JsonProperty("Creatinine")
    private Double creatinine;
    @JsonProperty("Glucose")
    private Double glucose;
    @JsonProperty("Lactate")
    private Double lactate;
    @JsonProperty("Magnesium")
    private Double magnesium;
    @JsonProperty("Phosphate")
    private Double phosphate;
    @JsonProperty("Potassium")
    private Double potassium;
    @JsonProperty("Bilirubin_total")
    private Double bilirubinTotal;
    @JsonProperty("Hct")
    private Double hct;
    @JsonProperty("Hgb")
    private Double hgb;
    @JsonProperty("PTT")
    private Double ptt;
    @JsonProperty("WBC")
    private Double wbc;
    @JsonProperty("Fibrinogen")
    private Double fibrinogen;
    @JsonProperty("Platelets")
    private Double platelets;

    @JsonIgnore
    private String observationId;

    public String getObservationId() {
        return observationId;
    }
    public void setObservationId(String x){
        observationId = x;
    }

    @JsonProperty("HR")
    public Double getHr() {
        return hr;
    }

    @JsonProperty("HR")
    public void setHr(Double hr) {
        this.hr = hr;
    }

    @JsonProperty("O2Sat")
    public Double getO2Sat() {
        return o2Sat;
    }

    @JsonProperty("O2Sat")
    public void setO2Sat(Double o2Sat) {
        this.o2Sat = o2Sat;
    }

    @JsonProperty("Temp")
    public Double getTemp() {
        return temp;
    }

    @JsonProperty("Temp")
    public void setTemp(Double temp) {
        this.temp = temp;
    }

    @JsonProperty("SBP")
    public Double getSbp() {
        return sbp;
    }

    @JsonProperty("SBP")
    public void setSbp(Double sbp) {
        this.sbp = sbp;
    }

    @JsonProperty("MAP")
    public Double getMap() {
        return map;
    }

    @JsonProperty("MAP")
    public void setMap(Double map) {
        this.map = map;
    }

    @JsonProperty("DBP")
    public Double getDbp() {
        return dbp;
    }

    @JsonProperty("DBP")
    public void setDbp(Double dbp) {
        this.dbp = dbp;
    }

    @JsonProperty("Resp")
    public Double getResp() {
        return resp;
    }

    @JsonProperty("Resp")
    public void setResp(Double resp) {
        this.resp = resp;
    }

    @JsonProperty("EtCO2")
    public Double getEtCO2() {
        return etCO2;
    }

    @JsonProperty("EtCO2")
    public void setEtCO2(Double etCO2) {
        this.etCO2 = etCO2;
    }

    @JsonProperty("BaseExcess")
    public Double getBaseExcess() {
        return baseExcess;
    }

    @JsonProperty("BaseExcess")
    public void setBaseExcess(Double baseExcess) {
        this.baseExcess = baseExcess;
    }

    @JsonProperty("HCO3")
    public Double getHco3() {
        return hco3;
    }

    @JsonProperty("HCO3")
    public void setHco3(Double hco3) {
        this.hco3 = hco3;
    }

    @JsonProperty("FiO2")
    public Double getFiO2() {
        return fiO2;
    }

    @JsonProperty("FiO2")
    public void setFiO2(Double fiO2) {
        this.fiO2 = fiO2;
    }

    @JsonProperty("pH")
    public Double getpH() {
        return pH;
    }

    @JsonProperty("pH")
    public void setpH(Double pH) {
        this.pH = pH;
    }

    @JsonProperty("PaCO2")
    public Double getPaCO2() {
        return paCO2;
    }

    @JsonProperty("PaCO2")
    public void setPaCO2(Double paCO2) {
        this.paCO2 = paCO2;
    }

    @JsonProperty("SaO2")
    public Double getSaO2() {
        return saO2;
    }

    @JsonProperty("SaO2")
    public void setSaO2(Double saO2) {
        this.saO2 = saO2;
    }

    @JsonProperty("AST")
    public Double getAst() {
        return ast;
    }

    @JsonProperty("AST")
    public void setAst(Double ast) {
        this.ast = ast;
    }

    @JsonProperty("BUN")
    public Double getBun() {
        return bun;
    }

    @JsonProperty("BUN")
    public void setBun(Double bun) {
        this.bun = bun;
    }

    @JsonProperty("Alkalinephos")
    public Double getAlkalinephos() {
        return alkalinephos;
    }

    @JsonProperty("Alkalinephos")
    public void setAlkalinephos(Double alkalinephos) {
        this.alkalinephos = alkalinephos;
    }

    @JsonProperty("Calcium")
    public Double getCalcium() {
        return calcium;
    }

    @JsonProperty("Calcium")
    public void setCalcium(Double calcium) {
        this.calcium = calcium;
    }

    @JsonProperty("Chloride")
    public Double getChloride() {
        return chloride;
    }

    @JsonProperty("Chloride")
    public void setChloride(Double chloride) {
        this.chloride = chloride;
    }

    @JsonProperty("Creatinine")
    public Double getCreatinine() {
        return creatinine;
    }

    @JsonProperty("Creatinine")
    public void setCreatinine(Double creatinine) {
        this.creatinine = creatinine;
    }

    @JsonProperty("Glucose")
    public Double getGlucose() {
        return glucose;
    }

    @JsonProperty("Glucose")
    public void setGlucose(Double glucose) {
        this.glucose = glucose;
    }

    @JsonProperty("Lactate")
    public Double getLactate() {
        return lactate;
    }

    @JsonProperty("Lactate")
    public void setLactate(Double lactate) {
        this.lactate = lactate;
    }

    @JsonProperty("Magnesium")
    public Double getMagnesium() {
        return magnesium;
    }

    @JsonProperty("Magnesium")
    public void setMagnesium(Double magnesium) {
        this.magnesium = magnesium;
    }

    @JsonProperty("Phosphate")
    public Double getPhosphate() {
        return phosphate;
    }

    @JsonProperty("Phosphate")
    public void setPhosphate(Double phosphate) {
        this.phosphate = phosphate;
    }

    @JsonProperty("Potassium")
    public Double getPotassium() {
        return potassium;
    }

    @JsonProperty("Potassium")
    public void setPotassium(Double potassium) {
        this.potassium = potassium;
    }

    @JsonProperty("Bilirubin_total")
    public Double getBilirubinTotal() {
        return bilirubinTotal;
    }

    @JsonProperty("Bilirubin_total")
    public void setBilirubinTotal(Double bilirubinTotal) {
        this.bilirubinTotal = bilirubinTotal;
    }

    @JsonProperty("Hct")
    public Double getHct() {
        return hct;
    }

    @JsonProperty("Hct")
    public void setHct(Double hct) {
        this.hct = hct;
    }

    @JsonProperty("Hgb")
    public Double getHgb() {
        return hgb;
    }

    @JsonProperty("Hgb")
    public void setHgb(Double hgb) {
        this.hgb = hgb;
    }

    @JsonProperty("PTT")
    public Double getPtt() {
        return ptt;
    }

    @JsonProperty("PTT")
    public void setPtt(Double ptt) {
        this.ptt = ptt;
    }

    @JsonProperty("WBC")
    public Double getWbc() {
        return wbc;
    }

    @JsonProperty("WBC")
    public void setWbc(Double wbc) {
        this.wbc = wbc;
    }

    @JsonProperty("Fibrinogen")
    public Double getFibrinogen() {
        return fibrinogen;
    }

    @JsonProperty("Fibrinogen")
    public void setFibrinogen(Double fibrinogen) {
        this.fibrinogen = fibrinogen;
    }

    @JsonProperty("Platelets")
    public Double getPlatelets() {
        return platelets;
    }

    @JsonProperty("Platelets")
    public void setPlatelets(Double platelets) {
        this.platelets = platelets;
    }

}