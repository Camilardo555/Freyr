import { User, FamilyGroup, Debt, Payment, Invitation } from '../types';

const STORAGE_KEYS = {
  USERS: 'komorebi_users_v1',
  CURRENT_USER_ID: 'komorebi_current_user_id_v1',
  GROUPS: 'komorebi_groups_v1',
  DEBTS: 'komorebi_debts_v1',
  PAYMENTS: 'komorebi_payments_v1',
  INVITATIONS: 'komorebi_invitations_v1',
};

const INITIAL_USERS: User[] = [
  {
    id: 'user-juan',
    fullName: 'Juan Pérez',
    username: 'juan',
    email: 'juan@example.com',
    password: 'password123',
    avatarColor: '#69042A',
    createdAt: '2026-01-15T10:00:00Z',
  },
  {
    id: 'user-maria',
    fullName: 'María González',
    username: 'maria',
    email: 'maria@example.com',
    password: 'password123',
    avatarColor: '#9C27B0',
    createdAt: '2026-01-16T11:00:00Z',
  },
  {
    id: 'user-carlos',
    fullName: 'Carlos Rodríguez',
    username: 'carlos',
    email: 'carlos@example.com',
    password: 'password123',
    avatarColor: '#00897B',
    createdAt: '2026-02-01T09:30:00Z',
  },
];

const INITIAL_GROUPS: FamilyGroup[] = [
  {
    id: 'group-perez-gonzalez',
    name: 'Familia Pérez González',
    creatorId: 'user-juan',
    memberIds: ['user-juan', 'user-maria'],
    createdAt: '2026-01-20T14:00:00Z',
  },
];

const INITIAL_DEBTS: Debt[] = [
  {
    id: 'debt-laptop',
    name: 'Laptop',
    description: 'Work and study laptop monthly finance',
    amount: 3000000,
    assignedUserId: 'user-juan',
    familyGroupId: null, // Personal debt
    status: 'pending',
    createdAt: '2026-02-10T12:00:00Z',
    createdBy: 'user-juan',
  },
  {
    id: 'debt-internet',
    name: 'Internet Bill',
    description: 'Fiber optics high-speed internet monthly service',
    amount: 120000,
    assignedUserId: 'user-juan',
    familyGroupId: 'group-perez-gonzalez', // Family debt assigned to Juan
    status: 'pending',
    createdAt: '2026-02-15T08:30:00Z',
    createdBy: 'user-juan',
  },
  {
    id: 'debt-groceries',
    name: 'Bi-Weekly Groceries',
    description: 'Supermarket supplies and pantry items',
    amount: 350000,
    assignedUserId: null, // Entire family
    familyGroupId: 'group-perez-gonzalez',
    status: 'pending',
    createdAt: '2026-02-18T16:00:00Z',
    createdBy: 'user-maria',
  },
  {
    id: 'debt-gym',
    name: 'Gym Annual Membership',
    description: 'Fitness center access paid off',
    amount: 360000,
    assignedUserId: 'user-juan',
    familyGroupId: null, // Personal debt
    status: 'paid',
    createdAt: '2026-01-10T10:00:00Z',
    createdBy: 'user-juan',
  },
];

const INITIAL_PAYMENTS: Payment[] = [
  {
    id: 'pay-laptop-1',
    debtId: 'debt-laptop',
    userId: 'user-juan',
    amount: 1000000,
    date: '2026-02-15',
    description: 'Initial deposit & first installment',
    createdAt: '2026-02-15T14:00:00Z',
  },
  {
    id: 'pay-internet-1',
    debtId: 'debt-internet',
    userId: 'user-juan',
    amount: 60000,
    date: '2026-02-20',
    description: 'First half payment',
    createdAt: '2026-02-20T09:00:00Z',
  },
  {
    id: 'pay-groceries-1',
    debtId: 'debt-groceries',
    userId: 'user-maria',
    amount: 150000,
    date: '2026-02-22',
    description: 'Fresh produce and meat payment',
    createdAt: '2026-02-22T17:00:00Z',
  },
  {
    id: 'pay-gym-1',
    debtId: 'debt-gym',
    userId: 'user-juan',
    amount: 360000,
    date: '2026-01-12',
    description: 'Full upfront payment with discount',
    createdAt: '2026-01-12T11:00:00Z',
  },
];

const INITIAL_INVITATIONS: Invitation[] = [
  {
    id: 'inv-carlos-1',
    familyGroupId: 'group-perez-gonzalez',
    invitedUserId: 'user-carlos',
    invitedByUserId: 'user-juan',
    status: 'pending',
    createdAt: '2026-02-24T10:00:00Z',
  },
];

export const storage = {
  getUsers: (): User[] => {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.USERS);
      if (!data) {
        localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(INITIAL_USERS));
        return INITIAL_USERS;
      }
      return JSON.parse(data);
    } catch {
      return INITIAL_USERS;
    }
  },

  setUsers: (users: User[]) => {
    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(users));
  },

  getCurrentUserId: (): string | null => {
    return localStorage.getItem(STORAGE_KEYS.CURRENT_USER_ID) || 'user-juan';
  },

  setCurrentUserId: (id: string | null) => {
    if (id) {
      localStorage.setItem(STORAGE_KEYS.CURRENT_USER_ID, id);
    } else {
      localStorage.removeItem(STORAGE_KEYS.CURRENT_USER_ID);
    }
  },

  getGroups: (): FamilyGroup[] => {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.GROUPS);
      if (!data) {
        localStorage.setItem(STORAGE_KEYS.GROUPS, JSON.stringify(INITIAL_GROUPS));
        return INITIAL_GROUPS;
      }
      return JSON.parse(data);
    } catch {
      return INITIAL_GROUPS;
    }
  },

  setGroups: (groups: FamilyGroup[]) => {
    localStorage.setItem(STORAGE_KEYS.GROUPS, JSON.stringify(groups));
  },

  getDebts: (): Debt[] => {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.DEBTS);
      if (!data) {
        localStorage.setItem(STORAGE_KEYS.DEBTS, JSON.stringify(INITIAL_DEBTS));
        return INITIAL_DEBTS;
      }
      return JSON.parse(data);
    } catch {
      return INITIAL_DEBTS;
    }
  },

  setDebts: (debts: Debt[]) => {
    localStorage.setItem(STORAGE_KEYS.DEBTS, JSON.stringify(debts));
  },

  getPayments: (): Payment[] => {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.PAYMENTS);
      if (!data) {
        localStorage.setItem(STORAGE_KEYS.PAYMENTS, JSON.stringify(INITIAL_PAYMENTS));
        return INITIAL_PAYMENTS;
      }
      return JSON.parse(data);
    } catch {
      return INITIAL_PAYMENTS;
    }
  },

  setPayments: (payments: Payment[]) => {
    localStorage.setItem(STORAGE_KEYS.PAYMENTS, JSON.stringify(payments));
  },

  getInvitations: (): Invitation[] => {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.INVITATIONS);
      if (!data) {
        localStorage.setItem(STORAGE_KEYS.INVITATIONS, JSON.stringify(INITIAL_INVITATIONS));
        return INITIAL_INVITATIONS;
      }
      return JSON.parse(data);
    } catch {
      return INITIAL_INVITATIONS;
    }
  },

  setInvitations: (invitations: Invitation[]) => {
    localStorage.setItem(STORAGE_KEYS.INVITATIONS, JSON.stringify(invitations));
  },

  resetToDefault: () => {
    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(INITIAL_USERS));
    localStorage.setItem(STORAGE_KEYS.GROUPS, JSON.stringify(INITIAL_GROUPS));
    localStorage.setItem(STORAGE_KEYS.DEBTS, JSON.stringify(INITIAL_DEBTS));
    localStorage.setItem(STORAGE_KEYS.PAYMENTS, JSON.stringify(INITIAL_PAYMENTS));
    localStorage.setItem(STORAGE_KEYS.INVITATIONS, JSON.stringify(INITIAL_INVITATIONS));
    localStorage.setItem(STORAGE_KEYS.CURRENT_USER_ID, 'user-juan');
  },
};
