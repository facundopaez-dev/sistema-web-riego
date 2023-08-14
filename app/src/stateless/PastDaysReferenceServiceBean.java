package stateless;

import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import model.PastDaysReference;
import javax.persistence.NoResultException;

@Stateless
public class PastDaysReferenceServiceBean {

    /*
     * Estas constantes son utilizadas para limitar la
     * cantidad de valores utilizados como la cantidad
     * de dias pasados como referencia para calcular
     * la necesidad de agua de riego en la fecha actual
     * de un cultivo sembrado y en desarrollo en una
     * parcela
     */
    private final int LOWER_LIMIT_PAST_DAYS = 1;
    private final int UPPER_LIMIT_PAST_DAYS = 7;

    /*
     * Instance variables
     */
    @PersistenceContext(unitName = "swcar")
    private EntityManager entityManager;

    public int getLowerLimitPastDays() {
        return LOWER_LIMIT_PAST_DAYS;
    }

    public int getUpperLimitPastDays() {
        return UPPER_LIMIT_PAST_DAYS;
    }

    public void setEntityManager(EntityManager localEntityManager) {
        entityManager = localEntityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public PastDaysReference create(PastDaysReference newPastDaysReference) {
        getEntityManager().persist(newPastDaysReference);
        return newPastDaysReference;
    }

    /**
     * Elimina fisicamente un PastDaysReference asociado a un
     * usuario dado
     * 
     * @param userId
     * @return referencia a un objeto de tipo PastDaysReference
     * si se elimina el PastDaysReference correspondiente al
     * usuario con el ID dado de la base de datos subyacente, en
     * caso contrario null
     */
    public PastDaysReference removeByUserId(int userId) {
        PastDaysReference givenPastDaysReference = findByUserId(userId);

        if (givenPastDaysReference != null) {
            getEntityManager().remove(givenPastDaysReference);
            return givenPastDaysReference;
        }

        return null;
    }

    /**
     * @param userId
     * @param pastDaysReferenceId
     * @return entero que representa el valor que tiene el
     * PastDaysReferencia correspondiente al ID dado y asociado
     * a un usuario dado
     */
    public int getValue(int userId, int pastDaysReferenceId) {
        return find(userId, pastDaysReferenceId).getValue();
    }

    /**
     * @param userId
     * @param pastDaysReferenceId
     * @param modifiedPastDaysReference
     * @return referencia a un objeto de tipo PastDaysReference si se
     * modifica el PastDaysReference con el ID y el ID de usuario
     * provistos, null en caso contrario
     */
    public PastDaysReference modify(int userId, int pastDaysReferenceId, PastDaysReference modifiedPastDaysReference) {
        PastDaysReference chosenPastDaysReference = find(userId, pastDaysReferenceId);

        if (chosenPastDaysReference != null) {
            chosenPastDaysReference.setValue(modifiedPastDaysReference.getValue());
            return chosenPastDaysReference;
        }

        return null;
    }

    /**
     * @param userId
     * @param pastDaysReferenceId
     * @return referencia a un objeto de tipo PastDaysReference
     * correspondiente a un usuario dado en caso de existir en
     * la base de datos subyacente un PastDaysReference con el
     * ID dado asociado al usuario del ID dado. En caso contrario,
     * null.
     */
    public PastDaysReference find(int userId, int pastDaysReferenceId) {
        Query query = entityManager.createQuery("SELECT p FROM PastDaysReference p WHERE (p.id = :pastDaysReferenceId AND p.user.id = :userId)");
        query.setParameter("pastDaysReferenceId", pastDaysReferenceId);
        query.setParameter("userId", userId);

        PastDaysReference givenPastDaysReference = null;

        try {
            givenPastDaysReference = (PastDaysReference) query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return givenPastDaysReference;
    }

    /**
     * Hay que tener en cuenta que este metodo no arrojara la
     * excepcion NonUniqueResultException siempre y cuando el
     * atributo User del modelo de datos PastDaysReference este
     * configurado con la restriccion de unicidad. De lo contrario,
     * este metodo fallara, y, en consecuencia, tambien fallara
     * cada metodo que lo invoque, como el metodo removeByUserId,
     * por ejemplo.
     * 
     * @param userId
     * @return referencia a un objeto de tipo PastDaysReference
     * correspondiente a un usuario dado en caso de existir en
     * la base de datos subyacente. En caso contrario, null.
     */
    public PastDaysReference findByUserId(int userId) {
        Query query = entityManager.createQuery("SELECT p FROM PastDaysReference p WHERE p.user.id = :userId");
        query.setParameter("userId", userId);

        PastDaysReference givenPastDaysReference = null;

        try {
            givenPastDaysReference = (PastDaysReference) query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return givenPastDaysReference;
    }

    /**
     * @param userId
     * @return referencia a un objeto de tipo Collection que
     * contiene el PastDaysReference del usuario con el ID
     * dado
     */
    public Collection<PastDaysReference> findAll(int userId) {
        Query query = entityManager.createQuery("SELECT p FROM PastDaysReference p WHERE p.user.id = :userId");
        query.setParameter("userId", userId);

        return (Collection) query.getResultList();
    }

    /**
     * Comprueba la existencia de un PastDaysReference en la base
     * de datos subyacente. Retorna true si y solo si existe el
     * PastDaysReference con el ID dado.
     * 
     * @param id
     * @return true si el PastDaysReference con el ID dado existe
     * en la base de datos subyacente, false en caso contrario
     */
    public boolean checkExistence(int id) {
        return (getEntityManager().find(PastDaysReference.class, id) != null);
    }

    /**
     * Retorna true si y solo si un PastDaysReference pertenece a
     * un usuario
     * 
     * @param userId
     * @param pastDaysReferenceId
     * @return true si el PastDaysReference del ID dado pertenece
     * al usuario con el ID dado, en caso contrario false
     */
    public boolean checkUserOwnership(int userId, int pastDaysReferenceId) {
        return (find(userId, pastDaysReferenceId) != null);
    }

}
