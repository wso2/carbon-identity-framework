package org.wso2.carbon.identity.cors.mgt.core.advisorybanner;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.advisory.mgt.dao.AdminAdvisoryBannerDAO;
import org.wso2.carbon.admin.advisory.mgt.dto.AdminAdvisoryBannerDTO;
import org.wso2.carbon.admin.advisory.mgt.exception.AdminAdvisoryMgtException;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;
import org.wso2.carbon.identity.cors.mgt.core.internal.CORSManagementServiceHolder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to manage storage of the Admin Advisory Banner configurations in the configuration-mgt store.
 */
public class DBBasedAdminBannerDAO implements AdminAdvisoryBannerDAO {

    public static final String ADVISORY_BANNER_RESOURCE_TYPE = "ADMIN_ADVISORY_BANNER";
    public static final String ADVISORY_BANNER_RESOURCE_NAME = "ADMIN_ADVISORY_BANNER_RESOURCE";
    public static final String ENABLE_BANNER = "enableBanner";
    public static final String BANNER_CONTENT = "bannerContent";

    private static final Log LOG = LogFactory.getLog(DBBasedAdminBannerDAO.class);

    @Override
    public void saveAdminAdvisoryConfig(AdminAdvisoryBannerDTO adminAdvisoryBannerDTO, String tenantDomain)
            throws AdminAdvisoryMgtException {

        Resource resource = buildResourceFromAdvisoryBannerDTO(adminAdvisoryBannerDTO);
        try {
            getConfigurationManager().addResource(ADVISORY_BANNER_RESOURCE_TYPE, resource);
        } catch (ConfigurationManagementException e) {
            throw new AdminAdvisoryMgtException("Error occurred while saving advisory banner configuration.", e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Admin advisory banner configuration saved successfully in configuration-store for tenant: "
                    + tenantDomain);
        }
    }

    @Override
    public AdminAdvisoryBannerDTO loadAdminAdvisoryConfig(String tenantDomain) throws AdminAdvisoryMgtException {

        try {
            Resource resource = getConfigurationManager().getResource(ADVISORY_BANNER_RESOURCE_TYPE,
                    ADVISORY_BANNER_RESOURCE_TYPE);
            if (resource == null) {
                return null;
            }

            List<ResourceFile> resourceFiles = getConfigurationManager().getFiles(ADVISORY_BANNER_RESOURCE_TYPE,
                    ADVISORY_BANNER_RESOURCE_NAME);

            if (resourceFiles.isEmpty() || StringUtils.isBlank(resourceFiles.get(0).getId())) {
                return null;
            }
            InputStream inputStream = getConfigurationManager().getFileById
                    (ADVISORY_BANNER_RESOURCE_TYPE, ADVISORY_BANNER_RESOURCE_NAME, resourceFiles.get(0).getId());
            AdminAdvisoryBannerDTO adminAdvisoryBannerDTO = buildAdvisoryBannerDTOFromResource(resource, inputStream);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Admin advisory banner configuration loaded successfully from configuration-store for" +
                        " tenant: " + tenantDomain);
            }
            return adminAdvisoryBannerDTO;
        } catch (ConfigurationManagementException e) {
            throw new AdminAdvisoryMgtException("Error occurred while loading advisory banner configuration.", e);
        }
    }

    private AdminAdvisoryBannerDTO buildAdvisoryBannerDTOFromResource(Resource resource, InputStream inputStream)
            throws AdminAdvisoryMgtException {

        AdminAdvisoryBannerDTO adminAdvisoryBannerDTO = new AdminAdvisoryBannerDTO();
        adminAdvisoryBannerDTO.setEnableBanner(Boolean.parseBoolean(resource.getAttributes().get(0).getValue()));
        String bannerContent;

        try {
            bannerContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AdminAdvisoryMgtException("Error occurred while converting banner content to string.", e);
        }
        adminAdvisoryBannerDTO.setBannerContent(bannerContent);
        return adminAdvisoryBannerDTO;
    }

    private Resource buildResourceFromAdvisoryBannerDTO(AdminAdvisoryBannerDTO adminAdvisoryBannerDTO)
            throws AdminAdvisoryMgtException {

        Resource resource = new Resource(ADVISORY_BANNER_RESOURCE_NAME, ADVISORY_BANNER_RESOURCE_TYPE);
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(ENABLE_BANNER, String.valueOf(adminAdvisoryBannerDTO.getEnableBanner())));
        resource.setAttributes(attributes);

        ResourceFile file = new ResourceFile();
        file.setName(BANNER_CONTENT);
        try (InputStream inputStream = new ByteArrayInputStream(adminAdvisoryBannerDTO.getBannerContent().
                getBytes(StandardCharsets.UTF_8))) {
            file.setInputStream(inputStream);
        } catch (IOException e) {
            throw new AdminAdvisoryMgtException("Error occurred while converting banner content to input stream.", e);
        }
        List<ResourceFile> resourceFiles = new ArrayList<>();
        resourceFiles.add(file);
        resource.setFiles(resourceFiles);
        return resource;
    }

    private ConfigurationManager getConfigurationManager() {

        return CORSManagementServiceHolder.getInstance().getConfigurationManager();
    }
}
