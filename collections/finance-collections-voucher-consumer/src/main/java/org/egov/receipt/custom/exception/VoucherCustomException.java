package org.egov.receipt.custom.exception;

public class VoucherCustomException extends Exception{
	String status;
	String message;
	String descriptions;
public VoucherCustomException(String message) {
	super(message);
}
public VoucherCustomException(String status,String message,String descriptions) {
	this.status=status;
	this.message=message;
	this.descriptions=descriptions;
}
@Override
public String getMessage() {
	return "[status : "+status+", message : "+message+", description : "+descriptions+"]";
}

}
