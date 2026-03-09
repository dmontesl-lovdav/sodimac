package com.sodimac.cfdi.entity.fiscal;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tablerocontroltimbrado")
public class TableroControlTimbradoEntity {
	
	@EmbeddedId
	private TableroControlTimbradoId fechaTicketTienda;
	@Column(name = "CANAL")
	private Integer canal;
	@Column(name = "SUBTOTAL_POS")
	private String subtotalPos;
	@Column(name = "NUMERO_TICKET_POS")
	private String numeroTicketPos;
	@Column(name = "SUBTOTAL_FAC_GLO")
	private String subtotalFacGlo;
	@Column(name = "NUMERO_TICKET_FAC_GLO")
	private String numeroTicketFacGlo;
	@Column(name = "DIF_SUBTOTAL_POS_FAC_GLO")
	private String difSubtotalPosFacGlo;
	@Column(name = "DIF_TICKET_POS_FAC_GLO")
	private String difTicketPosFacGlo;
	@Column(name = "SUBTOTAL_FAC_CLI")
	private String subtotalFacCli;
	@Column(name = "NUMERO_TICKET_FAC_CLIE")
	private String numeroTicketFacClie;
	@Column(name = "SUBTOTAL_FAC_CLI_NC_GLO")
	private String subtotalFacCliNcGlo;
	@Column(name = "NUMERO_TICKET_FAC_CLI_NC_GLO")
	private String numeroTicketFacCliNcGlo;
	@Column(name = "DIF_SUBTOTAL_FAC_CLI_NC_GLO")
	private String difSubtotalFacCliNcGlo;
	@Column(name = "DIF_TICKET_FAC_CLI_NC_GLO")
	private String difTicketFacCliNcGlo;
	@Column(name = "SUBTOTAL_POS_DEV")
	private String subtotalPosDev;
	@Column(name = "NUMERO_TICKET_POS_DEV")
	private String numeroTicketPosDev;
	@Column(name = "SUBTOTAL_NC_GLO")
	private String subtotalNcGlo;
	@Column(name = "NUMERO_TICKET_NC_GLO")
	private String numeroTicketNcGlo;
	@Column(name = "DIF_SUBTOTAL_POS_DEV_NC_GLO")
	private String difSubtotalPosDevNcGlo;
	@Column(name = "DIF_TICKET_POS_DEV_NC_GLO")
	private String difTicketPosDevNcGlo;
	@Column(name = "SUBTOTAL_NC_CLI")
	private String subtotalNcCli;
	@Column(name = "NUMERO_TICKET_NC_CLI")
	private String numeroTicketNcCli;
	@Column(name = "SUBTOTAL_FAC_GLO_NC_CLI")
	private String subtotalFacGloNcCli;
	@Column(name = "NUMERO_TICKET_FAC_GLO_NC_CLI")
	private String numeroTicketFacGloNcCli;
	@Column(name = "DIF_SUBTOTAL_FAC_GLO_NC_CLI")
	private String difSubtotalFacGloNcCli;
	@Column(name = "DIF_TICKET_FAC_GLO_NC_CLI")
	private String difTicketFacGloNcCli;
	@Column(name = "FECHA_REGISTRO")
	private String fechaRegistro;

	public TableroControlTimbradoId getFechaTicketTienda() {
		return fechaTicketTienda;
	}
	public void setFechaTicketTienda(TableroControlTimbradoId fechaTicketTienda) {
		this.fechaTicketTienda = fechaTicketTienda;
	}
	public Integer getCanal() {
		return canal;
	}
	public void setCanal(Integer canal) {
		this.canal = canal;
	}
	public String getSubtotalPos() {
		return subtotalPos;
	}
	public void setSubtotalPos(String subtotalPos) {
		this.subtotalPos = subtotalPos;
	}
	public String getNumeroTicketPos() {
		return numeroTicketPos;
	}
	public void setNumeroTicketPos(String numeroTicketPos) {
		this.numeroTicketPos = numeroTicketPos;
	}
	public String getSubtotalFacGlo() {
		return subtotalFacGlo;
	}
	public void setSubtotalFacGlo(String subtotalFacGlo) {
		this.subtotalFacGlo = subtotalFacGlo;
	}
	public String getNumeroTicketFacGlo() {
		return numeroTicketFacGlo;
	}
	public void setNumeroTicketFacGlo(String numeroTicketFacGlo) {
		this.numeroTicketFacGlo = numeroTicketFacGlo;
	}
	public String getDifSubtotalPosFacGlo() {
		return difSubtotalPosFacGlo;
	}
	public void setDifSubtotalPosFacGlo(String difSubtotalPosFacGlo) {
		this.difSubtotalPosFacGlo = difSubtotalPosFacGlo;
	}
	public String getDifTicketPosFacGlo() {
		return difTicketPosFacGlo;
	}
	public void setDifTicketPosFacGlo(String difTicketPosFacGlo) {
		this.difTicketPosFacGlo = difTicketPosFacGlo;
	}
	public String getSubtotalFacCli() {
		return subtotalFacCli;
	}
	public void setSubtotalFacCli(String subtotalFacCli) {
		this.subtotalFacCli = subtotalFacCli;
	}
	public String getNumeroTicketFacClie() {
		return numeroTicketFacClie;
	}
	public void setNumeroTicketFacClie(String numeroTicketFacClie) {
		this.numeroTicketFacClie = numeroTicketFacClie;
	}
	public String getSubtotalFacCliNcGlo() {
		return subtotalFacCliNcGlo;
	}
	public void setSubtotalFacCliNcGlo(String subtotalFacCliNcGlo) {
		this.subtotalFacCliNcGlo = subtotalFacCliNcGlo;
	}
	public String getNumeroTicketFacCliNcGlo() {
		return numeroTicketFacCliNcGlo;
	}
	public void setNumeroTicketFacCliNcGlo(String numeroTicketFacCliNcGlo) {
		this.numeroTicketFacCliNcGlo = numeroTicketFacCliNcGlo;
	}
	public String getDifSubtotalFacCliNcGlo() {
		return difSubtotalFacCliNcGlo;
	}
	public void setDifSubtotalFacCliNcGlo(String difSubtotalFacCliNcGlo) {
		this.difSubtotalFacCliNcGlo = difSubtotalFacCliNcGlo;
	}
	public String getDifTicketFacCliNcGlo() {
		return difTicketFacCliNcGlo;
	}
	public void setDifTicketFacCliNcGlo(String difTicketFacCliNcGlo) {
		this.difTicketFacCliNcGlo = difTicketFacCliNcGlo;
	}
	public String getSubtotalPosDev() {
		return subtotalPosDev;
	}
	public void setSubtotalPosDev(String subtotalPosDev) {
		this.subtotalPosDev = subtotalPosDev;
	}
	public String getNumeroTicketPosDev() {
		return numeroTicketPosDev;
	}
	public void setNumeroTicketPosDev(String numeroTicketPosDev) {
		this.numeroTicketPosDev = numeroTicketPosDev;
	}
	public String getSubtotalNcGlo() {
		return subtotalNcGlo;
	}
	public void setSubtotalNcGlo(String subtotalNcGlo) {
		this.subtotalNcGlo = subtotalNcGlo;
	}
	public String getNumeroTicketNcGlo() {
		return numeroTicketNcGlo;
	}
	public void setNumeroTicketNcGlo(String numeroTicketNcGlo) {
		this.numeroTicketNcGlo = numeroTicketNcGlo;
	}
	public String getDifSubtotalPosDevNcGlo() {
		return difSubtotalPosDevNcGlo;
	}
	public void setDifSubtotalPosDevNcGlo(String difSubtotalPosDevNcGlo) {
		this.difSubtotalPosDevNcGlo = difSubtotalPosDevNcGlo;
	}
	public String getDifTicketPosDevNcGlo() {
		return difTicketPosDevNcGlo;
	}
	public void setDifTicketPosDevNcGlo(String difTicketPosDevNcGlo) {
		this.difTicketPosDevNcGlo = difTicketPosDevNcGlo;
	}
	public String getSubtotalNcCli() {
		return subtotalNcCli;
	}
	public void setSubtotalNcCli(String subtotalNcCli) {
		this.subtotalNcCli = subtotalNcCli;
	}
	public String getNumeroTicketNcCli() {
		return numeroTicketNcCli;
	}
	public void setNumeroTicketNcCli(String numeroTicketNcCli) {
		this.numeroTicketNcCli = numeroTicketNcCli;
	}
	public String getSubtotalFacGloNcCli() {
		return subtotalFacGloNcCli;
	}
	public void setSubtotalFacGloNcCli(String subtotalFacGloNcCli) {
		this.subtotalFacGloNcCli = subtotalFacGloNcCli;
	}
	public String getNumeroTicketFacGloNcCli() {
		return numeroTicketFacGloNcCli;
	}
	public void setNumeroTicketFacGloNcCli(String numeroTicketFacGloNcCli) {
		this.numeroTicketFacGloNcCli = numeroTicketFacGloNcCli;
	}
	public String getDifSubtotalFacGloNcCli() {
		return difSubtotalFacGloNcCli;
	}
	public void setDifSubtotalFacGloNcCli(String difSubtotalFacGloNcCli) {
		this.difSubtotalFacGloNcCli = difSubtotalFacGloNcCli;
	}
	public String getDifTicketFacGloNcCli() {
		return difTicketFacGloNcCli;
	}
	public void setDifTicketFacGloNcCli(String difTicketFacGloNcCli) {
		this.difTicketFacGloNcCli = difTicketFacGloNcCli;
	}
	public String getFechaRegistro() {
		return fechaRegistro;
	}
	public void setFechaRegistro(String fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}
	
	@Override
	public String toString() {
		return "TableroControlTimbradoCfdiEntity [fechaTicketTienda=" + fechaTicketTienda + ", canal=" + canal
				+ ", subtotalPos=" + subtotalPos + ", numeroTicketPos=" + numeroTicketPos + ", subtotalFacGlo="
				+ subtotalFacGlo + ", numeroTicketFacGlo=" + numeroTicketFacGlo + ", difSubtotalPosFacGlo="
				+ difSubtotalPosFacGlo + ", difTicketPosFacGlo=" + difTicketPosFacGlo + ", subtotalFacCli="
				+ subtotalFacCli + ", numeroTicketFacClie=" + numeroTicketFacClie + ", subtotalFacCliNcGlo="
				+ subtotalFacCliNcGlo + ", numeroTicketFacCliNcGlo=" + numeroTicketFacCliNcGlo
				+ ", difSubtotalFacCliNcGlo=" + difSubtotalFacCliNcGlo + ", difTicketFacCliNcGlo="
				+ difTicketFacCliNcGlo + ", subtotalPosDev=" + subtotalPosDev + ", numeroTicketPosDev="
				+ numeroTicketPosDev + ", subtotalNcGlo=" + subtotalNcGlo + ", numeroTicketNcGlo=" + numeroTicketNcGlo
				+ ", difSubtotalPosDevNcGlo=" + difSubtotalPosDevNcGlo + ", difTicketPosDevNcGlo="
				+ difTicketPosDevNcGlo + ", subtotalNcCli=" + subtotalNcCli + ", numeroTicketNcCli=" + numeroTicketNcCli
				+ ", subtotalFacGloNcCli=" + subtotalFacGloNcCli + ", numeroTicketFacGloNcCli="
				+ numeroTicketFacGloNcCli + ", difSubtotalFacGloNcCli=" + difSubtotalFacGloNcCli
				+ ", difTicketFacGloNcCli=" + difTicketFacGloNcCli + ", fechaRegistro=" + fechaRegistro + "]";
	}

}
