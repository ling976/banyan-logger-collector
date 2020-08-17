/**
 * 
 */
package com.banyan.logger.stream;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;

/**
 * @author 皇甫逸彬
 *
 */
public class BanyanFilePrintStream extends PrintStream {


	private PrintStream oldPrintStream;
	
	private BanyanFilePrintStreamHelper consoleStreamHelper;

	public BanyanFilePrintStream(OutputStream out) {
		super(out);
		this.oldPrintStream = (PrintStream) out;
	}

	public BanyanFilePrintStreamHelper getConsoleStreamHelper() {
		return consoleStreamHelper;
	}

	public void setConsoleStreamHelper(BanyanFilePrintStreamHelper consoleStreamHelper) {
		this.consoleStreamHelper = consoleStreamHelper;
		this.consoleStreamHelper.start();
		this.consoleStreamHelper.getBanyanFtpServer().syncFileThread();
	}
	
	public void setOldPrintStream(PrintStream oldPrintStream) {
		this.oldPrintStream = oldPrintStream;
	}

	@Override
	public synchronized void write(int b) {
		oldPrintStream.write(b);
	}

	@Override
	public synchronized void write(byte b[], int off, int len) {
		//使用原始的PrintStream打印到控制台
		oldPrintStream.write(b, off, len);
		//写入文件
		ByteBuffer buff = ByteBuffer.wrap(b);
		consoleStreamHelper.write(buff);
	}

}
