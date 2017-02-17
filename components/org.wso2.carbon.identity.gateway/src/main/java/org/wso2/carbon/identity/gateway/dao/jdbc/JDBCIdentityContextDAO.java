package org.wso2.carbon.identity.gateway.dao.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.jdbc.DataAccessException;
import org.wso2.carbon.identity.common.jdbc.JdbcTemplate;
import org.wso2.carbon.identity.gateway.api.context.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.api.exception.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.context.DemoAuthenticationContext;
import org.wso2.carbon.identity.gateway.dao.IdentityContextDAO;
import org.wso2.carbon.identity.gateway.processor.request.ClientAuthenticationRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class JDBCIdentityContextDAO extends IdentityContextDAO {

    private static final Logger logger = LoggerFactory.getLogger(JDBCIdentityContextDAO.class);

    private static volatile JDBCIdentityContextDAO instance = new JDBCIdentityContextDAO();

    private JdbcTemplate jdbcTemplate;

    private static final String KEY = "KEY";
    private static final String OPERATION = "OPERATION";
    private static final String SESSION_OBJECT = "SESSION_OBJECT";
    private static final String TIME_CREATED = "TIME_CREATED";

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private JDBCIdentityContextDAO() {

    }

    public static JDBCIdentityContextDAO getInstance() {
        return instance;
    }

    @Override
    public void put(String key, IdentityMessageContext identityMessageContext) {

        final String storeContext =
                "INSERT INTO IDN_CONTEXT " + "(KEY, OPERATION, SESSION_OBJECT, TIME_CREATED)"
                        + "VALUES (:" + KEY + ";, :" + OPERATION + ";, :" + SESSION_OBJECT + ";, :"
                        + TIME_CREATED + ";)";

        try {
            jdbcTemplate.executeInsert(storeContext, (namedPreparedStatement) -> {
                namedPreparedStatement.setString(KEY, key);
                namedPreparedStatement.setString(OPERATION, "STORE");
                namedPreparedStatement.setBlob(SESSION_OBJECT, identityMessageContext);
                namedPreparedStatement.setTimeStamp(TIME_CREATED, new Timestamp(new Date().getTime()));
            }, null, false);
        } catch (DataAccessException e) {
            throw new FrameworkRuntimeException("Error while storing session.", e);
        }
    }

    @Override
    public IdentityMessageContext get(String key) {

        final String retrieveContext =
                "SELECT " + "OPERATION, TIME_CREATED, SESSION_OBJECT FROM IDN_CONTEXT WHERE KEY = :" + KEY + "; " +
                        "ORDER BY TIME_CREATED DESC LIMIT 1";

        AtomicReference<IdentityMessageContext> identityMessageContextAtomicReference = new AtomicReference<>();

        try {
            jdbcTemplate.fetchSingleRecord(retrieveContext, (resultSet, rowNumber) -> {
                String operation = resultSet.getString(OPERATION);
                if ("STORE".equals(operation)) {
                    InputStream is = resultSet.getBinaryStream(SESSION_OBJECT);
                    if (is != null) {
                        ObjectInput ois = null;
                        try {
                            ois = new ObjectInputStream(is);
                            identityMessageContextAtomicReference.set((IdentityMessageContext) ois.readObject());
                        } catch (IOException | ClassNotFoundException e) {
                            logger.error("Error while trying to close ObjectInputStream.", e);
                        } finally {
                            if (ois != null) {
                                try {
                                    ois.close();
                                } catch (IOException e) {
                                    logger.error("Error while trying to close ObjectInputStream.", e);
                                }
                            }
                        }
                    }
                }
                return identityMessageContextAtomicReference.get();
            }, namedPreparedStatement -> {
                namedPreparedStatement.setString(KEY, key);
            });
        } catch (DataAccessException e) {
            throw new FrameworkRuntimeException("Error while retrieving session.", e);
        }
        return identityMessageContextAtomicReference.get();
    }

    @Override
    public void remove(String key) {

        final String deleteContext =
                "INSERT INTO IDN_CONTEXT " + "(KEY, OPERATION, TIME_CREATED)"
                        + "VALUES (:" + KEY + ";, :" + OPERATION + ";, :"
                        + TIME_CREATED + ";)";

        try {
            jdbcTemplate.executeInsert(deleteContext, (namedPreparedStatement) -> {
                namedPreparedStatement.setString(KEY, key);
                namedPreparedStatement.setString(OPERATION, "DELETE");
                namedPreparedStatement.setTimeStamp(TIME_CREATED, new Timestamp(new Date().getTime()));
            }, null, false);
        } catch (DataAccessException e) {
            throw new FrameworkRuntimeException("Error while storing session.", e);
        }
    }
}
