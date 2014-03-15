package es.uclm.sri.persistencia;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import es.uclm.sri.sis.KSistema;

public class ConnectionFactory {
	
	private static SqlSessionFactory sqlMapper;
    private static Reader reader; 

    static{
        try{
            reader = Resources.getResourceAsReader("es/uclm/sri/recursos/xml/sqlMapConfig.xml");
            sqlMapper = new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static SqlSessionFactory getSession(){
        return sqlMapper;
    }

}