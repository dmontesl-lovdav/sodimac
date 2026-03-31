package com.sodimac.bctfacturacion.entity.bct;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author david.montes
 */
@Entity
@Table(name = "sw_cem.trx_points")
public class TrxPointsEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "ID")
    private Integer id;
    
    @Column(name = "TRANSACTIONTYPE")
    private String transactiontype;
    
    @Column(name = "IDENTIFICATIONTYPEID")
    private Integer identificationtypeid;
    
    @Column(name = "DATE")
    @Temporal(TemporalType.DATE)
    private Date date;
    
    @Column(name = "BRANCH")
    private String branch;
    
    @Column(name = "POS")
    private String pos;
    
    @Column(name = "SEQUENCE")
    private String sequence;
    
    @Column(name = "POINTS")
    private Integer points;
    
    @Column(name = "REDEMPTEDPOINTS")
    private Integer redemptedpoints;
    
    @Column(name = "EXPIREDPOINTS")
    private Integer expiredpoints;
    
    @Column(name = "VOIDEDPOINTS")
    private Integer voidedpoints;
    
    @Column(name = "AVAILABLEPOINTS")
    private Integer availablepoints;
    
    @Column(name = "PROCESSED")
    private Integer processed;
    
    @Column(name = "ACCOUNTID")
    private Integer accountid;
    
    @Column(name = "EXPIRATIONDATE")
    @Temporal(TemporalType.DATE)
    private Date expirationdate;
    
    @Column(name = "PROCESSEDREDEMPTION")
    private Integer processedredemption;
    
    @Column(name = "CUSTOMERID")
    private Integer customerid;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTransactiontype() {
		return transactiontype;
	}

	public void setTransactiontype(String transactiontype) {
		this.transactiontype = transactiontype;
	}

	public Integer getIdentificationtypeid() {
		return identificationtypeid;
	}

	public void setIdentificationtypeid(Integer identificationtypeid) {
		this.identificationtypeid = identificationtypeid;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public Integer getRedemptedpoints() {
		return redemptedpoints;
	}

	public void setRedemptedpoints(Integer redemptedpoints) {
		this.redemptedpoints = redemptedpoints;
	}

	public Integer getExpiredpoints() {
		return expiredpoints;
	}

	public void setExpiredpoints(Integer expiredpoints) {
		this.expiredpoints = expiredpoints;
	}

	public Integer getVoidedpoints() {
		return voidedpoints;
	}

	public void setVoidedpoints(Integer voidedpoints) {
		this.voidedpoints = voidedpoints;
	}

	public Integer getAvailablepoints() {
		return availablepoints;
	}

	public void setAvailablepoints(Integer availablepoints) {
		this.availablepoints = availablepoints;
	}

	public Integer getProcessed() {
		return processed;
	}

	public void setProcessed(Integer processed) {
		this.processed = processed;
	}

	public Integer getAccountid() {
		return accountid;
	}

	public void setAccountid(Integer accountid) {
		this.accountid = accountid;
	}

	public Date getExpirationdate() {
		return expirationdate;
	}

	public void setExpirationdate(Date expirationdate) {
		this.expirationdate = expirationdate;
	}

	public Integer getProcessedredemption() {
		return processedredemption;
	}

	public void setProcessedredemption(Integer processedredemption) {
		this.processedredemption = processedredemption;
	}

	public Integer getCustomerid() {
		return customerid;
	}

	public void setCustomerid(Integer customerid) {
		this.customerid = customerid;
	}
    
}
