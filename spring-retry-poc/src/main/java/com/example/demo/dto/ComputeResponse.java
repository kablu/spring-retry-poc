package com.example.demo.dto;

 
 
public class ComputeResponse {

    private int input;
    private long output;
    private ResponseType responseType;
    private String message;
	public ComputeResponse(int input, long output, ResponseType responseType, String message) {
		super();
		this.input = input;
		this.output = output;
		this.responseType = responseType;
		this.message = message;
	}
    
    

}