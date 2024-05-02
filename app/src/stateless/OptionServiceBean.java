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
     * Persiste una opcion para una parcela
     * 
     * @return referencia a un objeto de tipo Option
     */
    public Option create() {
        Option newOption = new Option();
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
        Option chosenOption = find(id);

        if (chosenOption != null) {
            chosenOption.setSoilFlag(modifiedOption.getSoilFlag());
            chosenOption.setFlagMessageFieldCapacity(modifiedOption.getFlagMessageFieldCapacity());
            return chosenOption;
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
        Query query = getEntityManager().createQuery("SELECT o FROM Parcel p JOIN p.option o WHERE (p.user.id = :userId AND o.id = :optionId)");
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
