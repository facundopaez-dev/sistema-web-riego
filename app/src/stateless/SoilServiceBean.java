package stateless;

import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.NoResultException;
import model.Soil;

@Stateless
public class SoilServiceBean {

    @PersistenceContext(unitName = "swcar")
    private EntityManager entityManager;

    public void setEntityManager(EntityManager localEntityManager) {
        entityManager = localEntityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public Soil create(Soil newSoil) {
        getEntityManager().persist(newSoil);
        return newSoil;
    }

    /**
     * @param id
     * @param modifiedSoil
     * @return referencia a un objeto de tipo Soil que contiene las
     * modificaciones del suelo pasado como parametro en caso de
     * encontrarse en la base de datos subyacente el suelo con el
     * ID dado, null en caso contrario
     */
    public Soil modify(int id, Soil modifiedSoil) {
        Soil givenSoil = find(id);

        if (givenSoil != null) {
            givenSoil.setName(modifiedSoil.getName());
            givenSoil.setApparentSpecificWeight(modifiedSoil.getApparentSpecificWeight());
            givenSoil.setFieldCapacity(modifiedSoil.getFieldCapacity());
            givenSoil.setPermanentWiltingPoint(modifiedSoil.getPermanentWiltingPoint());
            givenSoil.setActive(modifiedSoil.getActive());
            return givenSoil;
        }

        return null;
    }

    /**
     * Elimina de forma logica de la base de datos subyacente el suelo
     * que tiene un identificador dado
     *
     * @param id
     * @return referencia a un objeto de tipo Soil en caso de eliminarse
     * de la base de datos subyacente el suelo correspondiente al
     * ID dado, en caso contrario null
     */
    public Soil remove(int id) {
        Soil givenSoil = find(id);

        if (givenSoil != null) {
            givenSoil.setActive(false);
            return givenSoil;
        }

        return null;
    }

    /**
     * Este metodo es para el menu de busqueda de un suelo en
     * la pagina web de lista de suelos.
     * 
     * @param soilName
     * @return referencia a un objeto de tipo Collection que
     * contiene el suelo o los suelos que tienen un nombre que
     * contiene parcial o totalmente un nombre dado. En caso
     * contrario, retorna un objeto de tipo Collection vacio.
     */
    public Collection<Soil> search(String soilName) {
        Query query = getEntityManager().createQuery("SELECT s FROM Soil s WHERE (UPPER(s.name) LIKE :givenNameSoil) ORDER BY s.name");
        query.setParameter("givenNameSoil", "%" + soilName.toUpperCase() + "%");

        Collection<Soil> givenSoil = null;

        try {
            givenSoil = (Collection) query.getResultList();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return givenSoil;
    }

    public Soil find(int id) {
        return getEntityManager().find(Soil.class, id);
    }

    /**
     * @return referencia a un objeto de tipo Collection que
     * contiene todos los suelos, tanto los eliminados
     * logicamente (inactivos) como los que no
     */
    public Collection<Soil> findAll() {
        Query query = getEntityManager().createQuery("SELECT s FROM Soil s ORDER BY s.id");
        return (Collection) query.getResultList();
    }

    /**
     * Retorna el suelo que tiene el nombre dado si y solo si
     * existe en la base de datos subyacente un suelo con el
     * nombre dado este activo o inactivo
     * 
     * @param name
     * @return referencia a un objeto de tipo Soil que representa
     * el suelo que tiene el nombre dado este activo o inactivo,
     * si existe en la base de datos subyacente. En caso contrario,
     * retorna null.
     */
    public Soil findByName(String name) {
        Query query = getEntityManager().createQuery("SELECT s FROM Soil s WHERE UPPER(s.name) = UPPER(:name)");
        query.setParameter("name", name);

        Soil givenSoil = null;

        try {
            givenSoil = (Soil) query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return givenSoil;
    }

    /**
     * Retorna los suelos activos que tienen un nombre que coincide con
     * el nombre dado
     * 
     * @param soilName
     * @return referencia a un objeto de tipo Collection que contiene
     * todos los suelos activos que tienen un nombre que coincide con
     * el nombre dado
     */
    public Collection<Soil> findActiveSoilByName(String soilName) {
        StringBuffer queryStr = new StringBuffer("SELECT s FROM Soil s");

        if (soilName != null) {
            queryStr.append(" WHERE (UPPER(s.name) LIKE :name AND s.active = TRUE)");
        }

        Query query = entityManager.createQuery(queryStr.toString() + " ORDER BY s.name");

        if (soilName != null) {
            query.setParameter("name", "%" + soilName.toUpperCase() + "%");
        }

        Collection<Soil> operators = (Collection) query.getResultList();
        return operators;
    }

    /**
     * Retorna el suelo que tiene el nombre dado y un ID distinto
     * al del suelo del ID dado, si y solo si existe en la base
     * de datos subyacente
     * 
     * @param id
     * @return referencia a un objeto de tipo Soil que representa
     * el suelo tiene un ID distinto al ID dado y un nombre igual
     * al nombre dado, si existe en la base de datos subyacente.
     * En caso contrario, null.
     */
    private Soil findRepeated(int id, String name) {
        /*
         * Esta consulta obtiene el suelo que tiene su nombre
         * igual al nombre de un suelo del conjunto de suelos
         * en el que NO esta el suelo del ID dado
         */
        Query query = getEntityManager().createQuery("SELECT s FROM Soil s WHERE (s.id != :id AND UPPER(s.name) = UPPER(:name))");
        query.setParameter("id", id);
        query.setParameter("name", name);

        Soil givenSoil = null;

        try {
            givenSoil = (Soil) query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return givenSoil;
    }

    /**
     * Comprueba la existencia de un suelo en la base de datos
     * subyacente. Retorna true si y solo si existe el suelo
     * con el ID dado.
     * 
     * @param id
     * @return true si el suelo con el ID dado existe en la
     * base de datos subyacente, false en caso contrario
     */
    public boolean checkExistence(int id) {
        return (getEntityManager().find(Soil.class, id) != null);
    }

    /**
     * Retorna true si y solo si existe un suelo con el nombre
     * dado en la base de datos subyacente
     * 
     * @param name
     * @return true si existe el suelo con el nombre dado en la base
     * de datos subyacente, false en caso contrario. Tambien retorna
     * false en el caso en el que el argumento tiene el valor null.
     */
    public boolean checkExistence(String name) {
        /*
         * Si el nombre del suelo tiene el valor null, se retorna
         * false, ya que realizar la busqueda de un suelo con un
         * nombre con este valor es similar a buscar un suelo
         * inexistente en la base de datos subyacente.
         * 
         * Con este control se evita realizar una consulta a la base
         * de datos comparando el nombre de un suelo con el valor null.
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
     * Retorna true si y solo si existe en la base de datos subyacente
     * un suelo o varios suelos que tienen un nombre que contiene
     * parcial o totalmente un nombre dado.
     * 
     * @param name
     * @return true si existe en la base de datos subyacente un suelo
     * o varios suelos que tienen un nombre que contiene parcial o
     * totalmente un nombre dado, false en caso contrario. Tambien
     * retorna false en el caso en el que el argumento tiene el valor
     * null.
     */
    public boolean checkExistenceForSearch(String name) {
        /*
         * Si el nombre del suelo tiene el valor null, se retorna
         * false, ya que realizar la busqueda de un suelo con un
         * nombre con este valor es similar a buscar un suelo
         * inexistente en la base de datos subyacente.
         * 
         * Con este control se evita realizar una consulta a la base
         * de datos comparando el nombre de un suelo con el valor null.
         * Si no se realiza este control y se realiza esta consulta a
         * la base de datos, ocurre la excepcion SQLSyntaxErrorException,
         * debido a que la comparacion de un atributo con el valor
         * null incumple la sintaxis del proveedor del motor de base
         * de datos.
         */
        if (name == null) {
            return false;
        }

        return (!search(name).isEmpty());
    }

    /**
     * Retorna true si y solo si en la base de datos subyacente
     * existe un suelo con un nombre igual al nombre dado
     * y un ID distinto al ID dado
     * 
     * @param id
     * @return true si en la base de datos subyacente existe un
     * suelo con un nombre igual al nombre dado y un ID distinto
     * al ID dado, en caso contrario false
     */
    public boolean checkRepeated(int id, String name) {
        return (findRepeated(id, name) != null);
    }

}
