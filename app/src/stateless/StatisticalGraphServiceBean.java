package stateless;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import model.StatisticalData;
import model.StatisticalGraph;
import util.UtilDate;

@Stateless
public class StatisticalGraphServiceBean {

    @PersistenceContext(unitName = "swcar")
    private EntityManager entityManager;

    public void setEntityManager(EntityManager localEntityManager) {
        entityManager = localEntityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public StatisticalGraph find(int id) {
        return getEntityManager().find(StatisticalGraph.class, id);
    }

    /**
     * @param newStatisticalGraph
     * @return referencia a un objeto de tipo StatisticalGraph persistido
     */
    public StatisticalGraph create(StatisticalGraph newStatisticalGraph) {
        entityManager.persist(newStatisticalGraph);
        return newStatisticalGraph;
    }

    /**
     * Modifica un grafico estadistico
     *
     * @param userId
     * @param statisticalGraphId
     * @param modifiedStatisticalGraph
     * @return referencia a un objeto de tipo StatisticalGraph si se
     * modifica el grafico estadistico con el ID y el ID de usuario
     * provistos, null en caso contrario
     */
    public StatisticalGraph modify(int userId, int statisticalGraphId, StatisticalGraph modifiedStatisticalGraph) {
        StatisticalGraph chosenStatisticalGraph = findByUserId(userId, statisticalGraphId);

        if (chosenStatisticalGraph != null) {
            chosenStatisticalGraph.setText(modifiedStatisticalGraph.getText());
            chosenStatisticalGraph.setAverage(modifiedStatisticalGraph.getAverage());
            chosenStatisticalGraph.setData(modifiedStatisticalGraph.getData());
            chosenStatisticalGraph.setLabels(modifiedStatisticalGraph.getLabels());
            return chosenStatisticalGraph;
        }

        return null;
    }

    /**
     * Elimina fisicamente un grafico estadistico de la base de datos
     * subyacente
     * 
     * @param id
     * @return referencia a un objeto de tipo StatisticalGraph en caso
     * de eliminarse de la base de datos subyacente el grafico
     * estadistico correspondiente al ID dado, en caso contrario null
     */
    public StatisticalGraph remove(int id) {
        StatisticalGraph statisticalGraph = find(id);

        if (statisticalGraph != null) {
            getEntityManager().remove(statisticalGraph);
            return statisticalGraph;
        }

        return null;
    }

    /**
     * Elimina fisicamente un grafico estadistico perteneciente a
     * una parcela de un usuario
     * 
     * @param userId
     * @param statisticalReportId
     * @return referencia a un objeto de tipo StatisticalGraph en
     * caso de eliminarse de la base de datos subyacente el grafico
     * estadistico que tiene el ID dado y que esta asociado a una
     * parcela de un usuario que tiene el ID de usuario dado, en
     * caso contrario null
     */
    public StatisticalGraph remove(int userId, int statisticalReportId) {
        StatisticalGraph statisticalGraph = findByUserId(userId, statisticalReportId);

        if (statisticalGraph != null) {
            getEntityManager().remove(statisticalGraph);
            return statisticalGraph;
        }

        return null;
    }

    /**
     * Retorna un grafico estadistico perteneciente a una de las
     * parcelas de un usuario si y solo si existe en la base de
     * datos subyacente
     * 
     * @param userId
     * @param statisticalGraphId
     * @return referencia a un objeto de tipo StatisticalGraph que
     * representa un grafico estadistico si existe en la base
     * de datos subyacente un grafico estadistico con el ID dado
     * asociada a una parcela perteneciente a un usuario con
     * el ID dado. En caso contrario, null.
     */
    public StatisticalGraph findByUserId(int userId, int statisticalGraphId) {
        Query query = getEntityManager().createQuery("SELECT s FROM StatisticalGraph s WHERE s.id = :statisticalGraphId AND s.parcel.user.id = :userId");
        query.setParameter("userId", userId);
        query.setParameter("statisticalGraphId", statisticalGraphId);

        StatisticalGraph statisticalGraph = null;

        try {
            statisticalGraph = (StatisticalGraph) query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return statisticalGraph;
    }

    /**
     * @param userId
     * @param parcelId
     * @param dateFrom
     * @param dateUntil
     * @param statisticalData
     * @return referencia a un objeto de tipo StatisticalGraph
     * si en la base de datos subyacente existe un grafico
     * estadistico con una fecha desde y una fecha hasta
     * asociado a una parcela de un usuario
     */
    public StatisticalGraph find(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil, StatisticalData statisticalData) {
        Query query = getEntityManager().createQuery("SELECT s FROM StatisticalGraph s JOIN s.parcel p WHERE (s.dateFrom = :dateFrom AND s.dateUntil = :dateUntil AND s.statisticalData = :statisticalData AND p.id = :parcelId AND p.user.id = :userId) ORDER BY s.id");
        query.setParameter("userId", userId);
        query.setParameter("parcelId", parcelId);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateUntil", dateUntil);
        query.setParameter("statisticalData", statisticalData);

        StatisticalGraph statisticalGraph = null;

        try {
            statisticalGraph = (StatisticalGraph) query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return statisticalGraph;
    }

    /**
     * Comprueba la existencia de un grafico estadistico en la base
     * de datos subyacente. Retorna true si y solo si existe el
     * grafico de barras con el ID dado.
     * 
     * @param id
     * @return true si el grafico estadistico con el ID dado existe
     * en la base de datos subyacente, false en caso contrario
     */
    public boolean checkExistence(int id) {
        return (getEntityManager().find(StatisticalGraph.class, id) != null);
    }

    /**
     * Retorna true si y solo si un grafico estadistico pertenece
     * a una parcela de un usuario
     * 
     * @param userId
     * @param statisticalGraphId
     * @return true si se encuentra el grafico estadistico con el
     * ID y el ID de usuario provistos, false en caso contrario
     */
    public boolean checkUserOwnership(int userId, int statisticalGraphId) {
        return (findByUserId(userId, statisticalGraphId) != null);
    }

    /**
     * Retorna true si y solo si existe un grafico estadistico
     * con una fecha desde, una fecha hasta, una parcela y un
     * dato estadistico
     * 
     * @param userId
     * @param parcelId
     * @param dateFrom
     * @param dateUntil
     * @param statisticalData
     * @return true si en la base de datos subyacente existe un
     * grafico estadistico con una fecha desde, una fecha hasta,
     * una parcela y un dato estadistico. En caso contrario,
     * false.
     */
    public boolean checkExistence(int userId, int parcelId, Calendar dateFrom, Calendar dateUntil, StatisticalData statisticalData) {
        /*
         * Si una de las variables de tipo por referencia de tipo
         * Calendar tiene el valor null, significa que una de las
         * fechas NO esta definida. En este caso, se retorna false,
         * ya que realizar la busqueda de un grafico estadistico con
         * una variable de tipo Calendar con el valor null es similar
         * a buscar un grafico estadistico inexistente en la base
         * de datos subyacente.
         * 
         * Con este control se evita realizar una consulta a la base
         * de datos comparando una o ambas fechas con el valor null.
         * Si no se realiza este control y se realiza esta consulta
         * a la base de datos, ocurre la excepcion SQLSyntaxErrorException,
         * debido a que la comparacion de un atributo con el valor
         * null incumple la sintaxis del proveedor del motor de base
         * de datos.
         */
        if (dateFrom == null || dateUntil == null) {
            return false;
        }

        return (find(userId, parcelId, dateFrom, dateUntil, statisticalData) != null);
    }

    public Page<StatisticalGraph> findAllPagination(int userId, Integer page, Integer cantPerPage, Map<String, String> parameters) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        Calendar calendarDate;

        // Genera el WHERE dinámicamente
        StringBuffer where = new StringBuffer(" WHERE 1=1 AND e.parcel.user.id = :userId");

        if (parameters != null) {

            for (String param : parameters.keySet()) {
                Method method;

                try {
                    method = StatisticalGraph.class.getMethod("get" + capitalize(param));

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
                        case "Parcel":
                            where.append(" AND UPPER(e.");
                            where.append(param);
                            where.append(".name");
                            where.append(") LIKE UPPER('%");
                            where.append(parameters.get(param));
                            where.append("%')");
                            break;
                        case "Calendar":

                            if (param.equals("dateFrom")) {
                                date = new Date(dateFormatter.parse(parameters.get(param)).getTime());
                                calendarDate = UtilDate.toCalendar(date);
                                where.append(" AND e.");
                                where.append(param);
                                where.append(" >= ");
                                where.append("'" + UtilDate.convertDateToYyyyMmDdFormat(calendarDate) + "'");
                            }

                            if (param.equals("dateUntil")) {
                                date = new Date(dateFormatter.parse(parameters.get(param)).getTime());
                                calendarDate = UtilDate.toCalendar(date);
                                where.append(" AND e.");
                                where.append(param);
                                where.append(" <= ");
                                where.append("'" + UtilDate.convertDateToYyyyMmDdFormat(calendarDate) + "'");
                            }

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
        Query countQuery = entityManager.createQuery("SELECT COUNT(e.id) FROM " + StatisticalGraph.class.getSimpleName() + " e" + where.toString());
        countQuery.setParameter("userId", userId);

        // Pagina
        Query query = entityManager.createQuery("FROM " + StatisticalGraph.class.getSimpleName() + " e" + where.toString() + " ORDER BY e.id DESC");
        query.setMaxResults(cantPerPage);
        query.setFirstResult((page - 1) * cantPerPage);
        query.setParameter("userId", userId);

        Integer count = ((Long) countQuery.getSingleResult()).intValue();
        Integer lastPage = (int) Math.ceil((double) count / (double) cantPerPage);

        // Arma la respuesta
        Page<StatisticalGraph> resultPage = new Page<StatisticalGraph>(page, count, page > 1 ? page - 1 : page, page < lastPage ? page + 1 : lastPage, lastPage, query.getResultList());
        return resultPage;
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

}
