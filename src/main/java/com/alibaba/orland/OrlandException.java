/*
 * Copyright 1999-2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.orland;

/**
 *
 * @author tiger
 */
public class OrlandException extends Exception{

	public OrlandException(String message) {
		super(message);
	}

	public OrlandException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
