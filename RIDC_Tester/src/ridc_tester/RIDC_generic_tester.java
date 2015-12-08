package ridc_tester;
import oracle.stellent.ridc.IdcClientManager;
import oracle.stellent.ridc.IdcContext;
import oracle.stellent.ridc.IdcClientException;
import oracle.stellent.ridc.IdcClient;
import oracle.stellent.ridc.protocol.ServiceResponse;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.TransferFile;
import oracle.stellent.ridc.model.DataResultSet;
import oracle.stellent.ridc.model.DataObject;

import java.util.Random;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import java.util.Iterator;

public class RIDC_generic_tester
{

	private static String m_property_file = "config.properties";
	private static String m_connectionString = "idc://<host>:<port>";
	private static String m_user = "";
	private static String m_host = "";
	private static String m_port = "";
	private static String m_service = "";
	private static Configuration m_config;

    public static void main(String[] args) throws ConfigurationException
    {

		processArgs(args);
		processPropertiesFile();

		System.out.println("\nridc_generic_tester ...\n");
		System.out.println("connectionString: " + m_connectionString);
		System.out.println("user: " + m_user);
		System.out.println("service: " + m_service + "\n");

        try {
			run_service();

        } catch (IdcClientException e) {
            e.printStackTrace ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
	}

	private static void run_service() throws IdcClientException, IOException
	{
		Date startTime, endTime;

		startTime = new Date();

		// create the manager
        IdcClientManager manager = new IdcClientManager ();

		// build a client that will communicate using the intradoc protocol
        IdcClient idcClient = manager.createClient (m_connectionString);

		// create an identity
        IdcContext userContext = new IdcContext (m_user);

		// get the binder
        DataBinder binder = idcClient.createBinder ();

		// populate the binder with the parameters
		binder.putLocal("IdcService", m_service);

		for (Iterator keys = m_config.getKeys("idcservice"); keys.hasNext();)
		{
			String key = (String) keys.next();
			String[] keyParts = StringUtils.split(key, ".");
			System.out.println(keyParts[1] + "=" + m_config.getString(key));
			binder.putLocal(keyParts[1], m_config.getString(key));
		}

		// execute the request
		ServiceResponse response = idcClient.sendRequest (userContext, binder);

        //Display the status of the service call
        System.out.println ("\nResults ...\n");
        System.out.println (response.getResponseAsString());

		// close the response
		response.close ();

		endTime = new Date();
        printElapsedTime(m_service, startTime, endTime);
    }

	private static void printElapsedTime(String action, Date startTime, Date endTime)
	{
        long elapsedTime = endTime.getTime() - startTime.getTime();
        System.out.println(action + " elapsed time: " + elapsedTime + " milliseconds\n");
	}

	private static void processPropertiesFile() throws ConfigurationException
	{

		m_config = new PropertiesConfiguration(m_property_file);
		Boolean config_error = false;

		m_host = m_config.getString("ridc.host");
		m_port = m_config.getString("ridc.port");
		m_user = m_config.getString("ridc.user");
		m_service = m_config.getString("ridc.idcservice");

		if (m_host == null) {
			System.out.println("ridc.host not specified");
			config_error = true;
		}
		if (m_port == null) {
			System.out.println("ridc.port not specified");
			config_error = true;
		}
		if (m_service == null) {
			System.out.println("ridc.idcservice not specified");
			config_error = true;
		}
		if (m_user == null) {
			m_user = "sysadmin";
		}

		m_connectionString = "idc://" + m_host  + ":" + m_port;

		if (config_error) {
			System.exit(-1);
		}
	}

	private static void processArgs(String[] args)
	{
		if (args.length > 1) {
			printUsageHelp ();
		}
		else {
			if (args.length == 1) {
				m_property_file = args[0];
			}
        }
	}

   private static void printUsageHelp () {
        System.out.println("Usage: ridc_generic_tester <proprty file name>, default property file name is config.properties");
    }

 }