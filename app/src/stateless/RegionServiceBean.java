package stateless;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Calendar;
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

    /**
     * Este metodo es para el menu de busqueda de una region en
     * la pagina web de lista de regiones.
     * 
     * @param regionName
     * @return referencia a un objeto de tipo Collection que
     * contiene la region o las regiones que tienen un nombre
     * que contiene parcial o totalmente un nombre dado. En caso
     * contrario, retorna un objeto de tipo Collection vacio.
     */
    public Collection<Region> search(String regionName) {
        Query query = getEntityManager().createQuery("SELECT r FROM Region r WHERE (UPPER(r.name) LIKE :givenNameRegion) ORDER BY r.name");
        query.setParameter("givenNameRegion", "%" + regionName.toUpperCase() + "%");

        Collection<Region> givenRegion = null;

        try {
            givenRegion = (Collection) query.getResultList();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return givenRegion;
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
     * Retorna las regiones activas que tienen un nombre que coincide
     * con el nombre de region dado. Este metodo es para el ingreso de
     * la region en el formulario de creacion y modificacion de un dato
     * asociado a una region, como un cultivo, por ejemplo.
     * 
     * @param regionName
     * @return referencia a un objeto de tipo Collection que contiene
     * todas las regiones activas que tienen un nombre que coincide con
     * el nombre de region dado
     */
    public Collection<Region> findActiveRegionByName(String regionName) {
        StringBuffer queryStr = new StringBuffer("SELECT r FROM Region r");

        if (regionName != null) {
            queryStr.append(" WHERE (UPPER(r.name) LIKE :name AND r.active = TRUE)");
        }

        Query query = entityManager.createQuery(queryStr.toString());

        if (regionName != null) {
            query.setParameter("name", "%" + regionName.toUpperCase() + "%");
        }

        Collection<Region> operators = (Collection) query.getResultList();
        return operators;
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
     * Retorna true si y solo si existe en la base de datos subyacente
     * una region o varias regiones que tienen un nombre que contiene
     * parcial o totalmente un nombre dado.
     * 
     * @param name
     * @return true si existe en la base de datos subyacente una region
     * o varias regiones que tienen un nombre que contiene parcial o
     * totalmente un nombre dado, false en caso contrario. Tambien
     * retorna false en el caso en el que el argumento tiene el valor
     * null.
     */
    public boolean checkExistenceForSearch(String name) {
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

        return (!search(name).isEmpty());
    }

    /**
     * Retorna true si y solo si en la base de datos subyacente
     * existe una region con un nombre igual al nombre dado
     * y un ID distinto al ID dado
     * 
     * @param id
     * @return true si en la base de datos subyacente existe
     * una region con un nombre igual al nombre dado y un ID
     * distinto al ID dado, en caso contrario false
     */
    public boolean checkRepeated(int id, String name) {
        return (findRepeated(id, name) != null);
    }

    public Page<Region> findAllPagination(Integer page, Integer cantPerPage, Map<String, String> parameters) {
        // Genera el WHERE dinámicamente
        StringBuffer where = new StringBuffer(" WHERE 1=1");

        if (parameters != null) {

            for (String param : parameters.keySet()) {
                Method method;

                try {
                    method = Region.class.getMethod("get" + capitalize(param));

                    if (method == null || parameters.get(param) == null || parameters.get(param).isEmpty()) {
                        continue;
                    }

                    switch (method.getReturnType().getSimpleName()) {
                        case "String":
                            where.append(" AND UPPER(e.");
                            where.append(param);
                            where.append(") LIKE UPPER('%");
                            where.append(parameters.get(param));
                            where.append("%')");
                            break;
                        default:
                            where.append(" AND e.");
                            where.append(param);
                            where.append(" = ");
                            where.append(parameters.get(param));
                            break;
                    }

                } catch (NoSuchMethodException | SecurityException e) {
                    e.printStackTrace();
                }

            } // End for

        } // End if

        // Cuenta el total de resultados
        Query countQuery = entityManager.createQuery("SELECT COUNT(e.id) FROM " + Region.class.getSimpleName() + " e" + where.toString());

        // Pagina
        Query query = entityManager.createQuery("FROM " + Region.class.getSimpleName() + " e" + where.toString());
        query.setMaxResults(cantPerPage);
        query.setFirstResult((page - 1) * cantPerPage);
        Integer count = ((Long) countQuery.getSingleResult()).intValue();
        Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

        // Arma la respuesta
        Page<Region> resultPage = new Page<Region>(page, count, page > 1 ? page - 1 : page, page < lastPage ? page + 1 : lastPage, lastPage, query.getResultList());
        return resultPage;
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

}
