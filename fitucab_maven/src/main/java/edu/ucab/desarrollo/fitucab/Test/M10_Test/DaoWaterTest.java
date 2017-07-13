package edu.ucab.desarrollo.fitucab.Test.M10_Test;

import edu.ucab.desarrollo.fitucab.common.entities.EntityFactory;
import edu.ucab.desarrollo.fitucab.common.entities.Water;
import edu.ucab.desarrollo.fitucab.dataAccessLayer.DaoFactory;
import edu.ucab.desarrollo.fitucab.dataAccessLayer.M10.DaoWater;
import edu.ucab.desarrollo.fitucab.dataAccessLayer.M10.IDaoWater;
import edu.ucab.desarrollo.fitucab.common.entities.Sql;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by Raul A on 7/1/2017.
 */
public class DaoWaterTest {

    SimpleDateFormat _sdf2 = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat _sdf3 = new SimpleDateFormat("hh:mm:ss");
    Date fecha = new Date();

    @Before
    public void setUp() throws Exception {

        Sql _sql = new Sql();
        String insertPerson = "insert into person (personid, personusername, personpassword, personemail, personsex," +
                " personphone, personbirthdate) values (1, 'Sholom Meedendorpe', 'AOA', 'smeedendorpe0@goo.ne.jp'," +
                " 'f', '244-(874)954-1391', '1997-7-7');";
        _sql.sql(insertPerson);
        Sql _sql2 = new Sql();
        String insertWaterList1 = "INSERT INTO public.glass_historic(glasshistoricid, glasstime, glasstype, " +
                "fk_person) VALUES(201,'3000/10/02', 250, 1),(202,'3000/10/02', 300, 1),(203,'3000/10/02', 350, 1);";
        _sql2.sql(insertWaterList1);
        Sql _sql3 = new Sql();
        String insertWaterList2 = "INSERT INTO public.glass_historic(glasshistoricid, glasstime, glasstype, " +
                "fk_person) VALUES(301,'4000/10/02', 250, 1),(302,'4000/10/02', 300, 1),(303,'4000/10/02', 350, 1);";
        _sql3.sql(insertWaterList2);

    }

    @After
    public void tearDown() throws Exception {
        Sql _sql = new Sql();
        String deletePerson = "delete from person where personid = 1;";
        _sql.sql(deletePerson);

        Sql _sql3 = new Sql();
        String deleteWater = "TRUNCATE glass_historic RESTART IDENTITY;";
        _sql3.sql(deleteWater);
    }

    @Test
    public void create() throws Exception {
        Water _water = EntityFactory.createWater();
        _water.set_time(_sdf2.format(fecha));
        Water water = EntityFactory.createWater();
        IDaoWater daoWater = DaoFactory.instanceDaoWater(_water);
        _water.set_glasstype(200);
        _water.set_fkPerson(1);
            water = (Water) daoWater.create(_water);
            water = (Water) daoWater.create(_water);
        assertEquals ((long) water.get_cantidad(),2);
    }

    @Test
    public void getList() throws Exception {
        Water _water = EntityFactory.createWater();
        _water.set_fkPerson(1);
        _water.set_time("02/10/3000");
        ArrayList<Water> waterListCompare = new ArrayList<Water>();
        waterListCompare.add(new Water("3000/10/02",250));
        waterListCompare.add(new Water("3000/10/02",300));
        waterListCompare.add(new Water("3000/10/02",350));
        IDaoWater daoWater = DaoFactory.instanceDaoWater(_water);
        ArrayList<Water> waterList = daoWater.getList(_water);
        Arrays.deepEquals(waterList.toArray(), waterListCompare.toArray());
    }

    @Test
    public void getWater() throws Exception {
        Water _water = EntityFactory.createWater();
        _water.set_fkPerson(1);
        _water.set_time("02/10/3000");
        Water water = EntityFactory.createWater();
        Water waterCount = EntityFactory.createWater(900,3);
        IDaoWater daoWater = DaoFactory.instanceDaoWater(_water);
        water = (Water) daoWater.getWater(_water);
        assertTrue(waterCount.equals(water));
    }

    @Test
    public void deleteLast() throws Exception {
        Water _water = EntityFactory.createWater();
        Water waterResult = EntityFactory.createWater();
        _water.set_fkPerson(1);
        _water.set_time("02/10/3000");
        IDaoWater daoWater = DaoFactory.instanceDaoWater(_water);
        waterResult = (Water) daoWater.deleteLast(_water);
        assertTrue(waterResult.get_cantidad().equals(2));
    }

    @Test
    public void queryExecute() throws Exception {
        Water _water = EntityFactory.createWater();
        ResultSet rs;
        DaoWater daoWater = new DaoWater(_water);
        rs = daoWater.queryExecute("Select personsex from person where personid = 1");
        String sexo = "";
        while (rs.next()) {
            sexo = rs.getString("personsex");
        }
        assertTrue(sexo.equals("f"));
    }

    @Test
    public void addWaterResult() throws Exception {
        Water _water = EntityFactory.createWater();
        Water waterComparacion = EntityFactory.createWater();
        DaoWater daoWater = new DaoWater(_water);
        String dia = ("5000/10/02");
        String hora = ("10:10:10");
        String glassType = "200";
        String fkp = "1";
        ResultSet rs;
        Sql sql = new Sql();
        rs = sql.sql("Select res from m10_addwater('"+dia+" "+hora+"',"+glassType+","+fkp+")");
        _water = daoWater.addWaterResult(rs);
        assertEquals((int) _water.get_cantidad(),1);
    }

    @Test
    public void getWaterList() throws Exception {
        Water _water = EntityFactory.createWater();
        ResultSet rs;
        DaoWater daoWater = new DaoWater(_water);
        rs = daoWater.queryExecute("Select * from M10_GetListFecha(1 ,'3000/10/02')");
        ArrayList<Water> waterList = new ArrayList<Water>();
        waterList = daoWater.getWaterList(rs);
        ArrayList<Water> waterListCompare = new ArrayList<Water>();
        waterListCompare.add(new Water("3000/10/02",250));
        waterListCompare.add(new Water("3000/10/02",300));
        waterListCompare.add(new Water("3000/10/02",350));
        Arrays.deepEquals(waterList.toArray(), waterListCompare.toArray());
    }

    @Test
    public void getWaterItem() throws Exception {
        Water _water = EntityFactory.createWater();
        Water water = EntityFactory.createWater();
        water.set_cantidad(3);
        water.set_suma(900);
        ResultSet rs;
        DaoWater daoWater = new DaoWater(_water);
        rs = daoWater.queryExecute("Select * from M10_GetWaterGlass(1 ,'3000/10/02')");
        _water = daoWater.getWaterItem(rs);
        assertTrue(_water.equals(water));
    }

    @Test
    public void deletLastItem() throws Exception {
        Water _water = EntityFactory.createWater();
        ResultSet rs;
        DaoWater daoWater = new DaoWater(_water);
        rs = daoWater.queryExecute("Select * from M10_DeletWaterLast('4000/10/02',1)");
        _water = daoWater.deletLastItem(rs);
        assertEquals((int)_water.get_cantidad(),2);
    }

}