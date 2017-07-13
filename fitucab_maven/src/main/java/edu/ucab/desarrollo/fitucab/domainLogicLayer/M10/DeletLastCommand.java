package edu.ucab.desarrollo.fitucab.domainLogicLayer.M10;

import com.google.gson.Gson;
import edu.ucab.desarrollo.fitucab.common.entities.Entity;
import edu.ucab.desarrollo.fitucab.common.entities.Water;
import edu.ucab.desarrollo.fitucab.common.exceptions.MessageException;
import edu.ucab.desarrollo.fitucab.dataAccessLayer.DaoFactory;
import edu.ucab.desarrollo.fitucab.dataAccessLayer.M10.IDaoWater;
import edu.ucab.desarrollo.fitucab.domainLogicLayer.Command;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Comando para eliminar al ultimo que extiende de command
 */
public class DeletLastCommand extends Command {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(DeletLastCommand.class);

    Entity _water;
    public String returned;

    /**
     * constructor de deletcommand
     * @param water
     */
    public DeletLastCommand (Entity water){ _water = water; };

    public void execute() {

        logger.debug("Debug: Eliminando agua->Comando");

        IDaoWater daoWater = DaoFactory.instanceDaoWater(_water);

        try {

            Water water = (Water) daoWater.deleteLast(_water);
            Gson gson = new Gson();
            returned = gson.toJson(water);

        } catch (SQLException e) {


            e.printStackTrace();
            MessageException error = new MessageException(e, this.getClass().getSimpleName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());

            logger.error("Error: ", error.toString());

        }

    }
    public Entity Return(){
        return null;
    }
}

