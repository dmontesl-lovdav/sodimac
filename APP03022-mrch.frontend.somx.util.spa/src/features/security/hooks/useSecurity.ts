import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { securityService } from '../services/securityService';
import type { CreateRoleAttributePayload, CreateUserAttributePayload, SecurityFilters } from '../types';

export const securityKeys = {
  all: ['security'] as const,
  profileUser: (filters: SecurityFilters) => [...securityKeys.all, 'profile-user', filters] as const,
  roleUser: (filters: SecurityFilters) => [...securityKeys.all, 'role-user', filters] as const,
  rolePermission: (filters: SecurityFilters) => [...securityKeys.all, 'role-permission', filters] as const,
  userAttribute: (filters: SecurityFilters) => [...securityKeys.all, 'user-attribute', filters] as const,
  roleAttribute: (filters: SecurityFilters) => [...securityKeys.all, 'role-attribute', filters] as const,
  roleAttributes: (id: number, page: number, pageSize: number) =>
    [...securityKeys.all, 'role-attributes', id, page, pageSize] as const,
  profileModule: (filters: SecurityFilters) => [...securityKeys.all, 'profile-module', filters] as const,
  profileModuleProcess: (filters: SecurityFilters) => [...securityKeys.all, 'profile-module-process', filters] as const,
  applicationEvent: (filters: SecurityFilters) => [...securityKeys.all, 'application-event', filters] as const,
  profileUserAssignment: (id: number) => [...securityKeys.all, 'profile-user-assignment', id] as const,
  roleUserAssignment: (id: number) => [...securityKeys.all, 'role-user-assignment', id] as const,
  rolePermissionAssignment: (id: number) => [...securityKeys.all, 'role-permission-assignment', id] as const,
  profileModuleAssignment: (id: number) => [...securityKeys.all, 'profile-module-assignment', id] as const,
  profileModuleProcessAssignment: (id: number) => [...securityKeys.all, 'profile-module-process-assignment', id] as const,
  applicationEventAssignment: (id: number) => [...securityKeys.all, 'application-event-assignment', id] as const,
  userAttributes: (id: number, page: number, pageSize: number) =>
    [...securityKeys.all, 'user-attributes', id, page, pageSize] as const,
  attributeTypes: () => [...securityKeys.all, 'attribute-types'] as const,
};

export const useProfileUserSearch = (filters: SecurityFilters, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.profileUser(filters),
    queryFn: () => securityService.searchProfileUsers(filters),
    enabled,
  });

export const useRoleUserSearch = (filters: SecurityFilters, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.roleUser(filters),
    queryFn: () => securityService.searchRoleUsers(filters),
    enabled,
  });

export const useRolePermissionSearch = (filters: SecurityFilters, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.rolePermission(filters),
    queryFn: () => securityService.searchRolePermissions(filters),
    enabled,
  });

export const useUserAttributeSearch = (filters: SecurityFilters, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.userAttribute(filters),
    queryFn: () => securityService.searchUserAttributes(filters),
    enabled,
  });

export const useProfileModuleSearch = (filters: SecurityFilters, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.profileModule(filters),
    queryFn: () => securityService.searchProfileModules(filters),
    enabled,
  });

export const useProfileModuleProcessSearch = (filters: SecurityFilters, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.profileModuleProcess(filters),
    queryFn: () => securityService.searchProfileModuleProcesses(filters),
    enabled,
  });

export const useApplicationEventSearch = (filters: SecurityFilters, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.applicationEvent(filters),
    queryFn: () => securityService.searchApplicationEvents(filters),
    enabled,
  });

export const useProfileUserAssignment = (id: number, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.profileUserAssignment(id),
    queryFn: () => securityService.getProfileUserAssignment(id),
    enabled,
  });

export const useRoleUserAssignment = (id: number, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.roleUserAssignment(id),
    queryFn: () => securityService.getRoleUserAssignment(id),
    enabled,
  });

export const useRolePermissionAssignment = (id: number, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.rolePermissionAssignment(id),
    queryFn: () => securityService.getRolePermissionAssignment(id),
    enabled,
  });

export const useProfileModuleAssignment = (id: number, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.profileModuleAssignment(id),
    queryFn: () => securityService.getProfileModuleAssignment(id),
    enabled,
  });

export const useProfileModuleProcessAssignment = (id: number, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.profileModuleProcessAssignment(id),
    queryFn: () => securityService.getProfileModuleProcessAssignment(id),
    enabled,
  });

export const useApplicationEventAssignment = (id: number, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.applicationEventAssignment(id),
    queryFn: () => securityService.getApplicationEventAssignment(id),
    enabled,
  });

export const useSaveProfileUserAssignment = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, selectedIds }: { id: number; selectedIds: number[] }) =>
      securityService.saveProfileUserAssignment(id, selectedIds),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: securityKeys.all });
      queryClient.invalidateQueries({ queryKey: securityKeys.profileUserAssignment(variables.id) });
    },
  });
};

export const useSaveRoleUserAssignment = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, selectedIds }: { id: number; selectedIds: number[] }) =>
      securityService.saveRoleUserAssignment(id, selectedIds),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: securityKeys.all });
      queryClient.invalidateQueries({ queryKey: securityKeys.roleUserAssignment(variables.id) });
    },
  });
};

export const useSaveRolePermissionAssignment = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, selectedIds }: { id: number; selectedIds: number[] }) =>
      securityService.saveRolePermissionAssignment(id, selectedIds),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: securityKeys.all });
      queryClient.invalidateQueries({ queryKey: securityKeys.rolePermissionAssignment(variables.id) });
    },
  });
};

export const useSaveProfileModuleAssignment = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, selectedIds }: { id: number; selectedIds: number[] }) =>
      securityService.saveProfileModuleAssignment(id, selectedIds),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: securityKeys.all });
      queryClient.invalidateQueries({ queryKey: securityKeys.profileModuleAssignment(variables.id) });
    },
  });
};

export const useSaveProfileModuleProcessAssignment = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, selectedIds }: { id: number; selectedIds: number[] }) =>
      securityService.saveProfileModuleProcessAssignment(id, selectedIds),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: securityKeys.all });
      queryClient.invalidateQueries({ queryKey: securityKeys.profileModuleProcessAssignment(variables.id) });
    },
  });
};

export const useSaveApplicationEventAssignment = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, selectedIds }: { id: number; selectedIds: number[] }) =>
      securityService.saveApplicationEventAssignment(id, selectedIds),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: securityKeys.all });
      queryClient.invalidateQueries({ queryKey: securityKeys.applicationEventAssignment(variables.id) });
    },
  });
};

export const useUserAttributes = (userId: number, page: number, pageSize: number, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.userAttributes(userId, page, pageSize),
    queryFn: () => securityService.getUserAttributes(userId, page, pageSize),
    enabled,
  });

export const useAttributeTypes = () =>
  useQuery({
    queryKey: securityKeys.attributeTypes(),
    queryFn: () => securityService.getAttributeCatalog(),
  });

export const useCreateUserAttribute = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateUserAttributePayload) => securityService.createUserAttribute(payload),
    onSuccess: async (_, variables) => {
      await queryClient.invalidateQueries({ queryKey: securityKeys.all });
      await queryClient.invalidateQueries({
        queryKey: [...securityKeys.all, 'user-attributes', variables.userId],
      });
    },
  });
};

export const useDeleteUserAttribute = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ userId, attributeId }: { userId: number; attributeId: number }) =>
      securityService.deleteUserAttribute(userId, attributeId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: securityKeys.all });
      queryClient.invalidateQueries({ queryKey: [...securityKeys.all, 'user-attributes', variables.userId] });
    },
  });
};

export const useRoleAttributeSearch = (filters: SecurityFilters, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.roleAttribute(filters),
    queryFn: () => securityService.searchRoleAttributes(filters),
    enabled,
  });

export const useRoleAttributes = (roleId: number, page: number, pageSize: number, enabled: boolean) =>
  useQuery({
    queryKey: securityKeys.roleAttributes(roleId, page, pageSize),
    queryFn: () => securityService.getRoleAttributes(roleId, page, pageSize),
    enabled,
  });

export const useCreateRoleAttribute = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateRoleAttributePayload) => securityService.createRoleAttribute(payload),
    onSuccess: async (_, variables) => {
      await queryClient.invalidateQueries({ queryKey: securityKeys.all });
      await queryClient.invalidateQueries({
        queryKey: [...securityKeys.all, 'role-attributes', variables.roleId],
      });
    },
  });
};

export const useDeleteRoleAttribute = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ roleId, attributeId }: { roleId: number; attributeId: number }) =>
      securityService.deleteRoleAttribute(roleId, attributeId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: securityKeys.all });
      queryClient.invalidateQueries({ queryKey: [...securityKeys.all, 'role-attributes', variables.roleId] });
    },
  });
};
