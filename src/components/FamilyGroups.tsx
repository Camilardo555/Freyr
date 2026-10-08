import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { formatCurrency, formatDate } from '../utils/currency';
import {
  Users,
  Plus,
  UserPlus,
  Shield,
  Clock,
  CheckCircle2,
  Calendar,
  User,
  History,
  Trash2,
  ChevronRight,
  Send,
  AlertCircle
} from 'lucide-react';
import { FamilyGroup, Debt } from '../types';

export const FamilyGroups: React.FC = () => {
  const {
    currentUser,
    familyGroups,
    debts,
    users,
    openCreateGroupModal,
    openAddDebtModal,
    openAddPaymentModal,
    openDebtDetailModal,
    inviteUserToGroup,
    getDebtPaidAmount,
    getDebtRemainingAmount,
    getUserName,
    deleteDebt,
  } = useApp();

  const userGroups = familyGroups.filter((g) =>
    currentUser ? g.memberIds.includes(currentUser.id) : false
  );

  const [selectedGroupId, setSelectedGroupId] = useState<string>(() => userGroups[0]?.id || '');
  const [inviteInput, setInviteInput] = useState('');
  const [inviteError, setInviteError] = useState<string | null>(null);
  const [inviteSuccess, setInviteSuccess] = useState<string | null>(null);
  const [debtFilter, setDebtFilter] = useState<'all' | 'pending' | 'paid' | 'assigned_to_me'>('all');

  // Auto-select first group if selection is empty or invalidated
  const activeGroup =
    userGroups.find((g) => g.id === selectedGroupId) || userGroups[0] || null;

  const isCreator = activeGroup && currentUser && activeGroup.creatorId === currentUser.id;

  // Group members list
  const groupMembers = activeGroup
    ? users.filter((u) => activeGroup.memberIds.includes(u.id))
    : [];

  // Group debts
  const groupDebts = activeGroup
    ? debts.filter((d) => d.familyGroupId === activeGroup.id)
    : [];

  // Filtered debts
  const filteredGroupDebts = groupDebts.filter((debt) => {
    const remaining = getDebtRemainingAmount(debt.id);
    const isPaid = remaining === 0 || debt.status === 'paid';

    if (debtFilter === 'pending' && isPaid) return false;
    if (debtFilter === 'paid' && !isPaid) return false;
    if (debtFilter === 'assigned_to_me') {
      if (debt.assignedUserId !== currentUser?.id) return false;
    }
    return true;
  });

  // Financial aggregates for active group
  const groupTotalDebt = groupDebts.reduce((sum, d) => sum + d.amount, 0);
  const groupTotalPaid = groupDebts.reduce(
    (sum, d) => sum + getDebtPaidAmount(d.id),
    0
  );
  const groupTotalRemaining = Math.max(0, groupTotalDebt - groupTotalPaid);

  const handleSendInvite = (e: React.FormEvent) => {
    e.preventDefault();
    setInviteError(null);
    setInviteSuccess(null);

    if (!activeGroup) return;
    if (!inviteInput.trim()) {
      setInviteError('Please enter an email or username.');
      return;
    }

    const result = inviteUserToGroup(activeGroup.id, inviteInput.trim());
    if (!result.success) {
      setInviteError(result.error || 'Failed to send invitation.');
    } else {
      setInviteSuccess(`Invitation successfully sent to ${inviteInput.trim()}!`);
      setInviteInput('');
      setTimeout(() => setInviteSuccess(null), 3500);
    }
  };

  if (!currentUser) return null;

  return (
    <div className="space-y-8 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-[#69042A]">
            Family Groups
          </h1>
          <p className="text-sm text-[#2D1B22]/70 mt-1">
            Coordinate shared household expenses, assign debts, and track joint payments.
          </p>
        </div>

        <button
          onClick={openCreateGroupModal}
          className="self-start sm:self-center px-4 py-2.5 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-xl shadow-xs transition-colors flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          <span>Create Family Group</span>
        </button>
      </div>

      {userGroups.length === 0 ? (
        <div className="p-12 text-center bg-white rounded-2xl border border-[#E1CCD4] space-y-4">
          <div className="w-12 h-12 bg-[#FAF7F8] rounded-2xl flex items-center justify-center text-[#69042A] mx-auto border border-[#E1CCD4]/70">
            <Users className="w-6 h-6" />
          </div>
          <div className="space-y-1">
            <h3 className="text-base font-bold text-[#2D1B22]">No Family Groups Yet</h3>
            <p className="text-xs text-[#2D1B22]/60 max-w-md mx-auto">
              You are not a member of any family group yet. Create your own family group to invite members and assign shared debts, or ask a member to invite you.
            </p>
          </div>
          <button
            onClick={openCreateGroupModal}
            className="px-5 py-2.5 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] rounded-xl shadow-xs"
          >
            Create Your First Family Group
          </button>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Group Tabs Selector if multiple groups */}
          {userGroups.length > 1 && (
            <div className="flex items-center gap-2 overflow-x-auto pb-1">
              {userGroups.map((group) => (
                <button
                  key={group.id}
                  onClick={() => setSelectedGroupId(group.id)}
                  className={`px-4 py-2 text-xs font-semibold rounded-xl transition-all whitespace-nowrap ${
                    activeGroup?.id === group.id
                      ? 'bg-[#69042A] text-white shadow-xs'
                      : 'bg-white text-[#2D1B22]/70 hover:bg-[#FAF7F8] border border-[#E1CCD4]'
                  }`}
                >
                  {group.name}
                </button>
              ))}
            </div>
          )}

          {activeGroup && (
            <div className="space-y-6">
              {/* Active Group Hero & Financial Summary */}
              <div className="p-6 bg-white rounded-2xl border border-[#E1CCD4] shadow-xs space-y-6">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-[#FAF7F8]">
                  <div>
                    <div className="flex items-center gap-2">
                      <h2 className="text-xl font-bold text-[#2D1B22]">
                        {activeGroup.name}
                      </h2>
                      {isCreator && (
                        <span className="text-[11px] font-semibold text-[#69042A] bg-[#E1CCD4]/50 px-2.5 py-0.5 rounded-full">
                          You are the Creator
                        </span>
                      )}
                    </div>
                    <p className="text-xs text-[#2D1B22]/60 mt-0.5">
                      Created on {formatDate(activeGroup.createdAt)} · {groupMembers.length} active member{groupMembers.length === 1 ? '' : 's'}
                    </p>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => openAddDebtModal(activeGroup.id)}
                      className="px-4 py-2 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] rounded-xl shadow-xs transition-colors flex items-center gap-1.5"
                    >
                      <Plus className="w-3.5 h-3.5" />
                      <span>Add Family Debt</span>
                    </button>
                  </div>
                </div>

                {/* Group Financial Metrics */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  <div className="p-4 bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/60">
                    <span className="text-[11px] font-semibold uppercase tracking-wider text-[#2D1B22]/60 block">
                      Total Group Debts
                    </span>
                    <span className="text-xl font-bold font-mono tabular-nums text-[#2D1B22] mt-1 block">
                      {formatCurrency(groupTotalDebt)}
                    </span>
                    <span className="text-[10px] text-[#2D1B22]/50 mt-1 block">
                      {groupDebts.length} family commitment{groupDebts.length === 1 ? '' : 's'}
                    </span>
                  </div>

                  <div className="p-4 bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/60">
                    <span className="text-[11px] font-semibold uppercase tracking-wider text-[#2D1B22]/60 block">
                      Total Paid by Members
                    </span>
                    <span className="text-xl font-bold font-mono tabular-nums text-emerald-700 mt-1 block">
                      {formatCurrency(groupTotalPaid)}
                    </span>
                    <span className="text-[10px] text-[#2D1B22]/50 mt-1 block">
                      Recorded installments
                    </span>
                  </div>

                  <div className="p-4 bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/60">
                    <span className="text-[11px] font-semibold uppercase tracking-wider text-[#69042A] block">
                      Total Group Remaining
                    </span>
                    <span className="text-xl font-bold font-mono tabular-nums text-[#69042A] mt-1 block">
                      {formatCurrency(groupTotalRemaining)}
                    </span>
                    <span className="text-[10px] text-[#2D1B22]/50 mt-1 block">
                      Open shared balance
                    </span>
                  </div>
                </div>

                {/* Members list & Invite row */}
                <div className="pt-2 space-y-4">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                    <h3 className="text-xs font-bold uppercase tracking-wider text-[#2D1B22]/70">
                      Family Members ({groupMembers.length})
                    </h3>

                    {/* Member Avatars */}
                    <div className="flex items-center gap-2 overflow-x-auto">
                      {groupMembers.map((member) => (
                        <div
                          key={member.id}
                          className="flex items-center gap-1.5 px-2.5 py-1 bg-[#FAF7F8] border border-[#E1CCD4]/70 rounded-full text-xs"
                        >
                          <div
                            className="w-5 h-5 rounded-full text-white flex items-center justify-center text-[10px] font-bold uppercase"
                            style={{ backgroundColor: member.avatarColor || '#69042A' }}
                          >
                            {member.fullName.slice(0, 1)}
                          </div>
                          <span className="font-medium text-[#2D1B22]">
                            {member.fullName}
                          </span>
                          {member.id === activeGroup.creatorId && (
                            <span className="text-[9px] font-semibold text-[#69042A] uppercase">
                              (Admin)
                            </span>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Creator Invite Box */}
                  <form
                    onSubmit={handleSendInvite}
                    className="p-3.5 bg-[#FAF7F8] border border-[#E1CCD4]/70 rounded-xl space-y-2"
                  >
                    <div className="flex items-center gap-2">
                      <UserPlus className="w-4 h-4 text-[#69042A]" />
                      <span className="text-xs font-semibold text-[#2D1B22]">
                        Invite Family Member to "{activeGroup.name}"
                      </span>
                    </div>

                    <div className="flex gap-2">
                      <input
                        type="text"
                        value={inviteInput}
                        onChange={(e) => setInviteInput(e.target.value)}
                        placeholder="Enter username or email (e.g. carlos or maria@example.com)"
                        className="flex-1 px-3 py-1.5 bg-white border border-[#E1CCD4] rounded-lg text-xs text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:border-[#69042A]"
                      />
                      <button
                        type="submit"
                        className="px-3.5 py-1.5 bg-[#69042A] hover:bg-[#520320] text-white rounded-lg text-xs font-semibold transition-colors flex items-center gap-1 shadow-xs"
                      >
                        <Send className="w-3 h-3" />
                        <span>Send Invite</span>
                      </button>
                    </div>

                    {inviteError && (
                      <p className="text-[11px] text-rose-700 font-medium">{inviteError}</p>
                    )}
                    {inviteSuccess && (
                      <p className="text-[11px] text-emerald-700 font-medium">
                        {inviteSuccess}
                      </p>
                    )}
                  </form>
                </div>
              </div>

              {/* Family Debts Section */}
              <div className="space-y-4">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <h3 className="text-base font-bold text-[#69042A]">
                    Shared Family Debts
                  </h3>

                  {/* Filters */}
                  <div className="flex items-center gap-1 p-1 bg-white rounded-xl border border-[#E1CCD4]">
                    <button
                      onClick={() => setDebtFilter('all')}
                      className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors ${
                        debtFilter === 'all'
                          ? 'bg-[#FAF7F8] text-[#69042A] border border-[#E1CCD4]/60'
                          : 'text-[#2D1B22]/60 hover:text-[#2D1B22]'
                      }`}
                    >
                      All ({groupDebts.length})
                    </button>
                    <button
                      onClick={() => setDebtFilter('pending')}
                      className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors ${
                        debtFilter === 'pending'
                          ? 'bg-[#FAF7F8] text-[#69042A] border border-[#E1CCD4]/60'
                          : 'text-[#2D1B22]/60 hover:text-[#2D1B22]'
                      }`}
                    >
                      Pending
                    </button>
                    <button
                      onClick={() => setDebtFilter('assigned_to_me')}
                      className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors ${
                        debtFilter === 'assigned_to_me'
                          ? 'bg-[#FAF7F8] text-[#69042A] border border-[#E1CCD4]/60'
                          : 'text-[#2D1B22]/60 hover:text-[#2D1B22]'
                      }`}
                    >
                      Assigned to Me
                    </button>
                    <button
                      onClick={() => setDebtFilter('paid')}
                      className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors ${
                        debtFilter === 'paid'
                          ? 'bg-[#FAF7F8] text-[#69042A] border border-[#E1CCD4]/60'
                          : 'text-[#2D1B22]/60 hover:text-[#2D1B22]'
                      }`}
                    >
                      Paid
                    </button>
                  </div>
                </div>

                {filteredGroupDebts.length === 0 ? (
                  <div className="p-8 text-center bg-white rounded-2xl border border-[#E1CCD4] space-y-2">
                    <p className="text-xs text-[#2D1B22]/60">
                      No family debts found for this filter.
                    </p>
                    <button
                      onClick={() => openAddDebtModal(activeGroup.id)}
                      className="px-3.5 py-1.5 text-xs font-semibold text-[#69042A] bg-[#E1CCD4]/40 hover:bg-[#E1CCD4]/70 rounded-lg transition-colors"
                    >
                      + Add Family Debt
                    </button>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {filteredGroupDebts.map((debt) => {
                      const paid = getDebtPaidAmount(debt.id);
                      const remaining = getDebtRemainingAmount(debt.id);
                      const isPaid = remaining === 0 || debt.status === 'paid';
                      const percent = Math.min(100, Math.round((paid / debt.amount) * 100));
                      const assignedUser = users.find((u) => u.id === debt.assignedUserId);

                      return (
                        <div
                          key={debt.id}
                          className="p-5 bg-white rounded-2xl border border-[#E1CCD4] hover:border-[#69042A]/50 transition-all flex flex-col justify-between space-y-4 shadow-xs"
                        >
                          <div>
                            <div className="flex items-start justify-between gap-2">
                              <div className="space-y-1">
                                <div className="flex items-center gap-2">
                                  <h4 className="text-base font-bold text-[#2D1B22]">
                                    {debt.name}
                                  </h4>
                                  {isPaid ? (
                                    <span className="text-[11px] font-semibold text-emerald-800 bg-emerald-50 px-2 py-0.5 rounded-full flex items-center gap-1">
                                      <CheckCircle2 className="w-3 h-3" />
                                      Paid
                                    </span>
                                  ) : (
                                    <span className="text-[11px] font-semibold text-[#69042A] bg-[#E1CCD4]/50 px-2 py-0.5 rounded-full flex items-center gap-1">
                                      <Clock className="w-3 h-3" />
                                      Pending
                                    </span>
                                  )}
                                </div>
                                {debt.description && (
                                  <p className="text-xs text-[#2D1B22]/60">{debt.description}</p>
                                )}
                              </div>

                              <button
                                onClick={() => deleteDebt(debt.id)}
                                title="Delete family debt"
                                className="p-1.5 text-[#2D1B22]/40 hover:text-rose-700 hover:bg-rose-50 rounded-lg transition-colors"
                              >
                                <Trash2 className="w-4 h-4" />
                              </button>
                            </div>

                            {/* Responsible person badge */}
                            <div className="mt-2.5 flex items-center gap-2 text-xs">
                              <span className="text-[#2D1B22]/50">Assigned to:</span>
                              <span className="font-semibold text-[#69042A] bg-[#FAF7F8] px-2 py-0.5 rounded border border-[#E1CCD4]/60">
                                {assignedUser
                                  ? `${assignedUser.fullName} (@${assignedUser.username})`
                                  : 'Entire Family (Shared)'}
                              </span>
                            </div>
                          </div>

                          {/* Financial figures (Prompt example match: Internet Bill, Total $120,000, Paid $60,000, Remaining $60,000) */}
                          <div className="p-3.5 bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/60 space-y-2">
                            <div className="grid grid-cols-3 gap-2 text-xs">
                              <div>
                                <span className="text-[#2D1B22]/50 block text-[11px]">Total</span>
                                <span className="font-bold font-mono tabular-nums text-[#2D1B22]">
                                  {formatCurrency(debt.amount)}
                                </span>
                              </div>
                              <div>
                                <span className="text-[#2D1B22]/50 block text-[11px]">Paid</span>
                                <span className="font-bold font-mono tabular-nums text-emerald-700">
                                  {formatCurrency(paid)}
                                </span>
                              </div>
                              <div>
                                <span className="text-[#2D1B22]/50 block text-[11px]">Remaining</span>
                                <span className="font-bold font-mono tabular-nums text-[#69042A]">
                                  {formatCurrency(remaining)}
                                </span>
                              </div>
                            </div>

                            {/* Progress bar */}
                            <div className="space-y-1 pt-1">
                              <div className="w-full bg-white h-2 rounded-full overflow-hidden border border-[#E1CCD4]/40">
                                <div
                                  className="h-full bg-[#69042A] rounded-full transition-all duration-300"
                                  style={{ width: `${percent}%` }}
                                />
                              </div>
                              <div className="flex justify-between text-[10px] text-[#2D1B22]/50">
                                <span>{percent}% Paid</span>
                                <span>{formatCurrency(remaining)} remaining</span>
                              </div>
                            </div>
                          </div>

                          {/* Card Actions */}
                          <div className="pt-1 flex items-center justify-between gap-2 border-t border-[#FAF7F8]">
                            <button
                              onClick={() => openDebtDetailModal(debt)}
                              className="px-3 py-1.5 text-xs font-semibold text-[#2D1B22]/70 hover:text-[#69042A] hover:bg-[#FAF7F8] rounded-xl transition-colors flex items-center gap-1"
                            >
                              <History className="w-3.5 h-3.5" />
                              <span>History</span>
                            </button>

                            {!isPaid && (
                              <button
                                onClick={() => openAddPaymentModal(debt)}
                                className="px-3.5 py-1.5 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-xl shadow-xs transition-colors flex items-center gap-1"
                              >
                                <Plus className="w-3.5 h-3.5" />
                                <span>Add Payment</span>
                              </button>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
