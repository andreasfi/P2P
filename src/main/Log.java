package main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;


public class Log {
	
	private Logger log = Logger.getLogger("Log");
	private FileHandler fh;

	public Log()
	{
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatterServer = new SimpleDateFormat("MM-yy");
		String dateDuJour = formatterServer.format(currentDate.getTime());
		
		String pathFileLog = "./Log-" + dateDuJour + ".log";
		
		
		try
		{
			fh = new FileHandler("C:/Users/Samuel/Documents/GitHub/P2P/src/main/MyLogFile.log");
			log.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			
		}
		
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public Logger getLog()
	{
		return this.log;
	}
	
	public static class SocketFormatter extends Formatter {

		public SocketFormatter() {
			super();
		}

		public String format(LogRecord record) {

			// Create a StringBuffer to contain the formatted record
			StringBuffer sb = new StringBuffer();
			
			String retour = "";
			
			// Get the date from the LogRecord and add it to the buffer
			Date date = new Date(record.getMillis());
			/*sb.append(date.toString());
			sb.append(";");*/
			retour += date.toString() +"; ";

			/*sb.append(record.getSourceClassName());
			sb.append(";");*/
			retour += record.getSourceClassName() + "; ";

			// Get the level name and add it to the buffer
			/*sb.append(record.getLevel().getName());
			sb.append(";");*/
			
			retour += record.getLevel();

		/*	sb.append(formatMessage(record));
			sb.append("\r\n");*/
			
			retour += formatMessage(record);

			return retour;

		}

	}
	
}
