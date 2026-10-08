import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { X, Users, Check } from 'lucide-react';

export const CreateGroupModal: React.FC = () => {
  const { isCreateGroupModalOpen, closeCreateGroupModal, createFamilyGroup, setActiveTab } =
    useApp();
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);

  if (!isCreateGroupModalOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!name.trim()) {
      setError('Please provide a name for the family group.');
      return;
    }

    createFamilyGroup(name);
    setName('');
    closeCreateGroupModal();
    setActiveTab('groups');
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-md w-full border border-[#E1CCD4] shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        <div className="px-6 py-4 border-b border-[#E1CCD4]/70 flex items-center justify-between bg-[#FAF7F8]">
          <div>
            <h2 className="text-base font-bold text-[#69042A]">Create Family Group</h2>
            <p className="text-xs text-[#2D1B22]/60">
              Share, assign and track expenses with family members
            </p>
          </div>
          <button
            onClick={closeCreateGroupModal}
            className="p-1.5 rounded-lg text-[#2D1B22]/50 hover:text-[#69042A] hover:bg-[#E1CCD4]/30 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 text-xs font-medium">
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
              Family Group Name
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Familia Gómez, Casa Principal, Los Martínez"
              className="w-full px-3.5 py-2.5 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
            />
          </div>

          <div className="p-3 bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/60 text-xs text-[#2D1B22]/70 space-y-1">
            <p className="font-semibold text-[#69042A]">As creator, you will be able to:</p>
            <p>• Invite family members by username or email</p>
            <p>• Add family debts and assign them to specific members</p>
            <p>• Keep a shared record of all payments and balances</p>
          </div>

          <div className="pt-2 flex items-center justify-end gap-2.5">
            <button
              type="button"
              onClick={closeCreateGroupModal}
              className="px-4 py-2 text-xs font-semibold text-[#2D1B22]/70 hover:text-[#2D1B22] rounded-xl hover:bg-[#FAF7F8] transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-xl shadow-xs transition-colors flex items-center gap-1.5"
            >
              <Check className="w-3.5 h-3.5" />
              <span>Create Group</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
