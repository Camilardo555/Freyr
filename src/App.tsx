/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { Navbar } from './components/Navbar';
import { AuthView } from './components/AuthView';
import { Dashboard } from './components/Dashboard';
import { PersonalFinances } from './components/PersonalFinances';
import { FamilyGroups } from './components/FamilyGroups';
import { InvitationsView } from './components/InvitationsView';

// Modals
import { AddDebtModal } from './components/AddDebtModal';
import { AddPaymentModal } from './components/AddPaymentModal';
import { DebtDetailModal } from './components/DebtDetailModal';
import { CreateGroupModal } from './components/CreateGroupModal';
import { InvitationsModal } from './components/InvitationsModal';
import { ProfileModal } from './components/ProfileModal';

const AppContent: React.FC = () => {
  const { currentUser, activeTab } = useApp();

  if (!currentUser) {
    return <AuthView />;
  }

  return (
    <div className="min-h-screen bg-[#FAF7F8] flex flex-col text-[#2D1B22] selection:bg-[#E1CCD4] selection:text-[#69042A]">
      <Navbar />

      <main className="flex-1 pb-16">
        {activeTab === 'dashboard' && <Dashboard />}
        {activeTab === 'personal' && <PersonalFinances />}
        {activeTab === 'groups' && <FamilyGroups />}
        {activeTab === 'invitations' && <InvitationsView />}
      </main>

      {/* Global Modals */}
      <AddDebtModal />
      <AddPaymentModal />
      <DebtDetailModal />
      <CreateGroupModal />
      <InvitationsModal />
      <ProfileModal />

      {/* Minimalist Footprint Footer */}
      <footer className="border-t border-[#E1CCD4]/60 py-6 bg-[#FAF7F8]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between text-xs text-[#2D1B22]/50 gap-2">
          <p>© {new Date().getFullYear()} Komorebi Finance · Clean Personal & Family Financial Ledger</p>
          <p>Manual records only · No banking credentials required</p>
        </div>
      </footer>
    </div>
  );
};

export default function App() {
  return (
    <AppProvider>
      <AppContent />
    </AppProvider>
  );
}
