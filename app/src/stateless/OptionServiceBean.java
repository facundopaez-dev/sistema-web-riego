package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.Option;

@Stateless
public class OptionServiceBean {

    @PersistenceContext(unitName = "swcar")
    private EntityManager entityManager;

    /*
     * Estas constantes son utilizadas para limitar la cantidad
     * de valores utilizados como la cantidad de dias pasados a
     * utilizar como referencia para calcular la necesidad de
     * agua de riego en la fecha actual de un cultivo sembrado
     * y en desarrollo en una parcela
     */
    private final int LOWER_LIMIT_PAST_DAYS = 1;
    private final int UPPER_LIMIT_PAST_DAYS = 7;

    /*
     * Esta constante es utilizada para calcular la necesidad de
     * agua de riego de un cultivo en la fecha actual a partir
     * del ultimo riego registrado en los ultimos treinta dias
     * anteriores a la fecha actual, si el usuario activa la
     * opcion correspondiente a este calculo de la necesidad de
     * agua de riego.
     */
    private final int THIRTY_DAYS = 30;

    public void setEntityManager(EntityManager localEntityManager) {
        entityManager = localEntityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public Option find(int id) {
        return getEntityManager().find(Option.class, id);
    }

    /**
     * Persiste una opcion que tiene el valor de la constante
     * UPPER_LIMIT_PAST_DAYS y el valor booleano false por defecto
     * 
     * @return referencia a un objeto de tipo Option
     */
    public Option create() {
        Option newOption = new Option();
        newOption.setPastDaysReference(UPPER_LIMIT_PAST_DAYS);
        entityManager.persist(newOption);

        return newOption;
    }

    /**
     * @param id
     * @param modifiedOption
     * @return referencia a un objeto de tipo Option que contiene las
     *         modificaciones de la opcion pasada como parametro en caso de
     *         encontrarse en la base de datos subyacente la opcion con el
     *         ID dado, null en caso contrario
     */
    public Option modify(int id, Option modifiedOption) {
        Option givenOption = find(id);

        if (givenOption != null) {
            givenOption.setPastDaysReference(modifiedOption.getPastDaysReference());
            givenOption.setThirtyDaysFlag(modifiedOption.getThirtyDaysFlag());
            return givenOption;
        }

        return null;
    }

    /**
     * Elimina fisicamente una opcion de la base de datos subyacente
     * 
     * @param id
     * @return referencia a un objeto de tipo Option en caso de eliminarse
     *         de la base de datos subyacente la opcion correspondiente al ID
     *         dado, en caso contrario null
     */
    public Option remove(int id) {
        Option givenOption = find(id);

        if (givenOption != null) {
            getEntityManager().remove(givenOption);
            return givenOption;
        }

        return null;
    }

    /**
     * @param givenOption
     * @return true si el valor de pastDaysReference de un objeto de
     * tipo Option esta entre los valores determinados por las constantes
     * LOWER_LIMIT_PAST_DAYS y UPPER_LIMIT_PAST_DAYS. En caso contrario,
     * false.
     */
    public boolean validatePastDaysReference(Option givenOption) {

        /*
         * Si el valor de pastDaysReference de un objeto de tipo Option
         * es menor al valor de la constante LOWER_LIMIT_PAST_DAYS o
         * mayor al valor de la constante UPPER_LIMIT_PAST_DAYS, es
         * invalido, por lo tanto, se retorna false
         */
        if (givenOption.getPastDaysReference() < LOWER_LIMIT_PAST_DAYS || givenOption.getPastDaysReference() > UPPER_LIMIT_PAST_DAYS) {
            return false;
        }

        return true;
    }

    /**
     * @return entero que representa el limite inferior del
     * atributo "cantidad de dias previos a la fecha actual
     * a utilizar para calcular la necesidad de agua de riego
     * de un cultivo en la fecha actual" de una opcion
     */
    public int getLowerLimitPastDays() {
        return LOWER_LIMIT_PAST_DAYS;
    }

    /**
     * @return entero que representa el limite superior del
     * atributo "cantidad de dias previos a la fecha actual
     * a utilizar para calcular la necesidad de agua de riego
     * de un cultivo en la fecha actual" de una opcion
     */
    public int getUpperLimitPastDays() {
        return UPPER_LIMIT_PAST_DAYS;
    }

    /**
     * El valor de la constante THIRTY_DAYS es para calcular la
     * necesidad de agua de riego de un cultivo en la fecha actual
     * a partir del ultimo riego registrado en los ultimos treinta
     * dias anteriores a la fecha actual. Esto se realiza si el
     * usuario activa la opcion correspondiente a este calculo
     * de la necesidad de agua de riego.
     * 
     * @return int que tiene el valor 30
     */
    public int getValueThirtyDays() {
        return THIRTY_DAYS;
    }

}
