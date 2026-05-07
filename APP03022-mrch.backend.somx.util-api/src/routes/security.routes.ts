import { Router } from 'express';
import * as securityController from '@/controllers/security.controller.js';

const router = Router();

router.get('/profile-users', securityController.searchProfileUsers);
router.get('/profile-modules', securityController.searchProfileModules);
router.get('/profile-module-processes', securityController.searchProfileModuleProcesses);
router.get('/application-events', securityController.searchApplicationEvents);

router.get('/profiles/:id/users', securityController.getProfileUserAssignment);
router.put('/profiles/:id/users', securityController.saveProfileUserAssignment);
router.get('/profiles/:id/modules', securityController.getProfileModuleAssignment);
router.put('/profiles/:id/modules', securityController.saveProfileModuleAssignment);
router.get('/profiles/:id/module-processes', securityController.getProfileModuleProcessAssignment);
router.put('/profiles/:id/module-processes', securityController.saveProfileModuleProcessAssignment);

router.get('/role-users', securityController.searchRoleUsers);
router.get('/roles/:id/users', securityController.getRoleUserAssignment);
router.put('/roles/:id/users', securityController.saveRoleUserAssignment);

router.get('/role-permissions', securityController.searchRolePermissions);
router.get('/roles/:id/permissions', securityController.getRolePermissionAssignment);
router.put('/roles/:id/permissions', securityController.saveRolePermissionAssignment);

router.get('/user-attributes', securityController.searchUserAttributes);
router.get('/users/:id/attributes', securityController.listUserAttributes);
router.post('/users/:id/attributes', securityController.createUserAttribute);
router.delete('/users/:id/attributes/:attributeId', securityController.removeUserAttribute);

router.get('/catalogs/attribute-types', securityController.listAttributeTypes);
router.get('/catalogs/attribute-values', securityController.listAttributeValuesByType);

router.delete('/user-details/cache', securityController.invalidateUserDetailsCache);
router.get('/user-details/:userKey', securityController.getUserDetailsByCatalogKey);
router.get('/user-attributes-by-key/:userKey', securityController.getUserAttributesByKey);

router.get('/user-catalog', securityController.searchUserCatalog);
router.get('/user-catalog/csv', securityController.exportUserCatalogCsv);

router.get('/users/:id/catalog-detail', securityController.getUserCatalogDetail);
router.get('/users/:userId/applications/:moduleId/events-catalog', securityController.getUserApplicationEventsCatalog);
router.put('/users/:userId/module-processes/:moduleProcessId', securityController.setUserModuleProcessAssigned);
router.post('/users/:id/profiles', securityController.appendUserProfile);

router.get('/modules/:id/processes', securityController.getModuleProcessAssignment);
router.put('/modules/:id/processes', securityController.saveModuleProcessAssignment);

export default router;
