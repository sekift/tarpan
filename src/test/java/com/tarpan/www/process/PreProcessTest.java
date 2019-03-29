package com.tarpan.www.process;

import static org.junit.Assert.*;

import org.junit.Test;

public class PreProcessTest {
	String str = "文字#NN";
	
	@Test
	public void sentimentLoadTest(){
		System.out.println(PreProcess.sentimentLoad());
	}
	
	@Test
	public void getWordTest(){
		System.out.println(PreProcess.getWord(str));
		assertTrue(PreProcess.getWord(str).equals("文字"));
	}
	
	@Test
	public void getLabelTest(){
		System.out.println(PreProcess.getLabel(str));
		assertTrue(PreProcess.getLabel(str).equals("NN"));
	}

	
}
