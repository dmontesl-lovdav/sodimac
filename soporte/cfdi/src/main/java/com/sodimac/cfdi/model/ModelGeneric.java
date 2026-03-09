/**
 * 
 */
package com.sodimac.cfdi.model;

/**
 * @author jfalvarez
 *
 */

public class ModelGeneric {

	private String messageGlobal;
	private String typeMessage;
	private boolean code;

	public String getMessageGlobal() {
		return messageGlobal;
	}

	public void setMessageGlobal(String messageGlobal) {
		this.messageGlobal = messageGlobal;
	}

	public String getTypeMessage() {
		return typeMessage;
	}

	public void setTypeMessage(String typeMessage) {
		this.typeMessage = typeMessage;
	}

	public boolean isCode() {
		return code;
	}

	public void setCode(boolean code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "ModelGeneric [messageGlobal=" + messageGlobal + ", typeMessage=" + typeMessage + ", code=" + code + "]";
	}

}
