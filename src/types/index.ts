export interface User {
  id: string;
  fullName: string;
  username: string;
  email: string;
  password?: string;
  avatarColor: string;
  createdAt: string;
}

export interface FamilyGroup {
  id: string;
  name: string;
  creatorId: string;
  memberIds: string[];
  createdAt: string;
}

export interface Debt {
  id: string;
  name: string;
  description?: string;
  amount: number;
  assignedUserId: string | null; // null means entire family / general group debt (or personal owner if familyGroupId is null)
  familyGroupId: string | null; // null means personal debt
  status: 'pending' | 'paid';
  createdAt: string;
  createdBy: string;
}

export interface Payment {
  id: string;
  debtId: string;
  userId: string;
  amount: number;
  date: string;
  description?: string;
  createdAt: string;
}

export interface Invitation {
  id: string;
  familyGroupId: string;
  invitedUserId: string;
  invitedByUserId: string;
  status: 'pending' | 'accepted' | 'declined';
  createdAt: string;
}

export interface FinancialSummary {
  pendingDebtsCount: number;
  paidDebtsCount: number;
  personalPaymentsTotal: number;
  familyDebtsPendingTotal: number;
  totalPendingAmount: number;
  totalPaidAmount: number;
}
