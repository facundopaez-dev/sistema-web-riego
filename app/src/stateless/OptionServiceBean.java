package stateless;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.NoResultException;
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
     * del ultimo riego registrado dentro de los treinta dias
     * anteriores a la fecha actual, si el usuario activa la
     * opcion correspondiente a este calculo de la necesidad de
     * agua de riego de un cultivo
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
     * UPPER_LIMIT_PAST_DAYS
     * 
     * @return referencia a un objeto de tipo Option
     */
    public Option create() {
        Option newOption = new Option();
        newOption.setPastDaysReference(UPPER_LIMIT_PAST_DAYS);
        newOption.setFlagMessageFieldCapacity(true);
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
            givenOption.setSoilFlag(modifiedOption.getSoilFlag());
            givenOption.setFlagLastIrrigationThirtyDays(modifiedOption.getFlagLastIrrigationThirtyDays());
            givenOption.setFlagMessageFieldCapacity(modifiedOption.getFlagMessageFieldCapacity());
            return givenOption;
        }

        return null;
    }

    /**
     * Establece en false la bandera suelo de una opcion
     * de parcela
     * 
     * @param id
     */
    public void unsetSoilFlag(int id) {
        Query query = entityManager.createQuery("UPDATE Option o SET o.soilFlag = 0 WHERE o.id = :optionId");
        query.setParameter("optionId", id);
        query.executeUpdate();
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
     * Retorna una opcion perteneciente a una de las parcelas
     * de un usuario si y solo si existe en la base de datos
     * subyacente
     * 
     * @param userId
     * @param optionId
     * @return referencia a un objeto de tipo Option que
     * representa una opcion si existe en la base de datos
     * subyacente una opcion con el ID dado asociada a una
     * parcela perteneciente a un usuario con el ID dado.
     * En caso contrario, null.
     */
    public Option findByUserId(int userId, int optionId) {
        Query query = getEntityManager().createQuery("SELECT o FROM User u JOIN u.parcels p JOIN p.option o WHERE (u.id = :userId AND o.id = :optionId)");
        query.setParameter("userId", userId);
        query.setParameter("optionId", optionId);

        Option parcelOption = null;

        try {
            parcelOption = (Option) query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return parcelOption;
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

    /**
     * Comprueba la existencia de una opcion en la base de datos
     * subyacente. Retorna true si y solo si existe la opcion con
     * el ID dado.
     * 
     * @param id
     * @return true si la opcion con el ID dado existe en la base
     * de datos subyacente, false en caso contrario
     */
    public boolean checkExistence(int id) {
        return (getEntityManager().find(Option.class, id) != null);
    }

    /**
     * Retorna true si y solo si una opcion pertenece a una parcela
     * de un usuario
     * 
     * @param userId
     * @param optionId
     * @return true si se encuentra la opcion con el ID y el ID de
     * usuario provistos, false en caso contrario
     */
    public boolean checkUserOwnership(int userId, int optionId) {
        return (findByUserId(userId, optionId) != null);
    }

}
