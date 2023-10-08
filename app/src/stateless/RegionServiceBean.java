package stateless;

import java.util.Calendar;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.NoResultException;
import model.Region;

@Stateless
public class RegionServiceBean {

    @PersistenceContext(unitName = "swcar")
    protected EntityManager entityManager;

    public void setEntityManager(EntityManager localEntityManager) {
        entityManager = localEntityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Persiste en la base de datos subyacente una instancia
     * de tipo Region
     * 
     * @param newRegion
     * @return referencia a un objeto de tipo Region
     */
    public Region create(Region newRegion) {
        getEntityManager().persist(newRegion);
        return newRegion;
    }

    /**
     * Elimina de forma logica de la base de datos subyacente la region
     * que tiene un identificador dado
     *
     * @param id
     * @return referencia a un objeto de tipo Region en caso de eliminarse
     * de la base de datos subyacente la region correspondiente al ID dado,
     * en caso contrario null
     */
    public Region remove(int id) {
        Region givenRegion = find(id);

        if (givenRegion != null) {
            givenRegion.setActive(false);
            return givenRegion;
        }

        return null;
    }

    /**
     * Modifica una region mediante su ID
     * 
     * @param id
     * @param modifiedRegion
     * @return referencia a un objeto de tipo Region que contiene las
     * modificaciones de la region pasada como parametro en caso de
     * encontrarse en la base de datos subyacente la region con el
     * ID dado, en caso contrario null
     */
    public Region modify(int id, Region modifiedRegion) {
        Region givenRegion = find(id);

        if (givenRegion != null) {
            givenRegion.setName(modifiedRegion.getName());
            givenRegion.setActive(modifiedRegion.getActive());
            return givenRegion;
        }

        return null;
    }

    /**
     * @param regionOne
     * @param regionTwo
     * @return true si la region uno tiene el mismo nombre que
     * la region dos, en caso contrario false
     */
    public boolean equals(Region regionOne, Region regionTwo) {
        return regionOne.getName().equals(regionTwo.getName());
    }

    public Region find(int id) {
        return getEntityManager().find(Region.class, id);
    }

    /**
     * Retorna la region que tiene el nombre dado si y solo si
     * existe en la base de datos subyacente una region con el
     * nombre dado
     * 
     * @param name
     * @return referencia a un objeto de tipo Region que representa
     * la region que tiene el nombre dado, si existe en la base
     * de datos subyacente. En caso contrario, retorna null.
     */
    public Region findByName(String name) {
        Query query = getEntityManager().createQuery("SELECT r FROM Region r WHERE UPPER(r.name) = UPPER(:name)");
        query.setParameter("name", name);

        Region givenRegion = null;

        try {
            givenRegion = (Region) query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return givenRegion;
    }

    /**
     * Retorna la region que tiene el nombre dado y un ID distinto
     * al de la region del ID dado, si y solo si existe en la base
     * de datos subyacente
     * 
     * @param id
     * @return referencia a un objeto de tipo Region que representa
     * la region tiene un ID distinto al ID dado y un nombre igual
     * al nombre dado, si existe en la base de datos subyacente.
     * En caso contrario, null.
     */
    private Region findRepeated(int id, String name) {
        /*
         * Esta consulta obtiene la region que tiene su nombre
         * igual al nombre de una region del conjunto de regiones
         * en el que NO esta la region del ID dado
         */
        Query query = getEntityManager().createQuery("SELECT r FROM Region r WHERE (r.id != :id AND UPPER(r.name) = UPPER(:name))");
        query.setParameter("id", id);
        query.setParameter("name", name);

        Region givenRegion = null;

        try {
            givenRegion = (Region) query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return givenRegion;
    }

    /**
     * @return referencia a un objeto de tipo Collection que
     * contiene todos las regiones, tanto las eliminadas
     * logicamente (inactivas) como los que no
     */
    public Collection<Region> findAll() {
        Query query = getEntityManager().createQuery("SELECT r FROM Region r ORDER BY r.id");
        return (Collection) query.getResultList();
    }

    /**
     * @return referencia a un objeto de tipo Collection que
     * contiene todos las regiones activas
     */
    public Collection<Region> findAllActive() {
        Query query = getEntityManager().createQuery("SELECT r FROM Region r WHERE r.active = TRUE ORDER BY r.id");
        return (Collection) query.getResultList();
    }

    /**
     * Comprueba la existencia de una region en la base de datos
     * subyacente. Retorna true si y solo si existe la region
     * con el ID dado.
     * 
     * @param id
     * @return true si la region con el ID dado existe en la
     * base de datos subyacente, false en caso contrario
     */
    public boolean checkExistence(int id) {
        return (getEntityManager().find(Region.class, id) != null);
    }

    /**
     * Retorna true si y solo si existe una region con el nombre
     * dado en la base de datos subyacente
     * 
     * @param name
     * @return true si existe la region con el nombre dado en la base
     * de datos subyacente, false en caso contrario. Tambien retorna
     * false en el caso en el que el argumento tiene el valor null.
     */
    public boolean checkExistence(String name) {
        /*
         * Si el nombre de la region tiene el valor null, se retorna
         * false, ya que realizar la busqueda de una region con un
         * nombre con este valor es similar a buscar una region
         * inexistente en la base de datos subyacente.
         * 
         * Con este control se evita realizar una consulta a la base
         * de datos comparando el nombre de una region con el valor null.
         * Si no se realiza este control y se realiza esta consulta a
         * la base de datos, ocurre la excepcion SQLSyntaxErrorException,
         * debido a que la comparacion de un atributo con el valor
         * null incumple la sintaxis del proveedor del motor de base
         * de datos.
         */
        if (name == null) {
            return false;
        }

        return (findByName(name) != null);
    }

    /**
     * Retorna true si y solo si en la base de datos subyacente
     * existe una region con un nombre igual al nombre dado
     * y un ID distinto al ID dado
     * 
     * @param id
     * @return true si en la base de datos subyacente existe un
     * una region con un nombre igual al nombre dado y un ID
     * distinto al ID dado, en caso contrario false
     */
    public boolean checkRepeated(int id, String name) {
        return (findRepeated(id, name) != null);
    }

}