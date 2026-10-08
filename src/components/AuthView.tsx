import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { ShieldCheck, UserPlus, LogIn, ArrowRight, Check } from 'lucide-react';

export const AuthView: React.FC = () => {
  const { login, register, users } = useApp();
  const [isRegistering, setIsRegistering] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Login form state
  const [loginIdentifier, setLoginIdentifier] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // Register form state
  const [fullName, setFullName] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const handleLoginSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!loginIdentifier.trim()) {
      setError('Please enter your email or username.');
      return;
    }
    const result = login(loginIdentifier, loginPassword);
    if (!result.success) {
      setError(result.error || 'Failed to log in.');
    }
  };

  const handleRegisterSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    const result = register({
      fullName,
      username,
      email,
      password,
    });

    if (!result.success) {
      setError(result.error || 'Failed to create account.');
    }
  };

  const handleQuickDemoLogin = (userId: string) => {
    const user = users.find((u) => u.id === userId);
    if (user) {
      login(user.username, user.password || 'password123');
    }
  };

  return (
    <div className="min-h-screen bg-[#FAF7F8] flex flex-col justify-center items-center py-12 px-4 sm:px-6 lg:px-8 selection:bg-[#E1CCD4] selection:text-[#69042A]">
      <div className="w-full max-w-md space-y-8">
        {/* Brand Header */}
        <div className="text-center space-y-2">
          <div className="mx-auto w-12 h-12 rounded-2xl bg-[#69042A] text-white flex items-center justify-center font-bold text-2xl shadow-md">
            F
          </div>
          <h1 className="text-3xl font-bold tracking-tight text-[#69042A]">
            Freyr
          </h1>
          <p className="text-sm text-[#2D1B22]/70">
            Minimalist personal & family financial debt and payment tracker
          </p>
        </div>

        {/* Auth Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-[#E1CCD4] p-6 sm:p-8 space-y-6">
          {/* Mode Switcher */}
          <div className="flex p-1 bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/70">
            <button
              type="button"
              onClick={() => {
                setIsRegistering(false);
                setError(null);
              }}
              className={`flex-1 py-2 text-xs font-semibold rounded-lg transition-all ${
                !isRegistering
                  ? 'bg-white text-[#69042A] shadow-xs'
                  : 'text-[#2D1B22]/60 hover:text-[#2D1B22]'
              }`}
            >
              Sign In
            </button>
            <button
              type="button"
              onClick={() => {
                setIsRegistering(true);
                setError(null);
              }}
              className={`flex-1 py-2 text-xs font-semibold rounded-lg transition-all ${
                isRegistering
                  ? 'bg-white text-[#69042A] shadow-xs'
                  : 'text-[#2D1B22]/60 hover:text-[#2D1B22]'
              }`}
            >
              Create Account
            </button>
          </div>

          {/* Error Message */}
          {error && (
            <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 text-xs font-medium">
              {error}
            </div>
          )}

          {/* Form */}
          {!isRegistering ? (
            <form onSubmit={handleLoginSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-[#2D1B22]/80 mb-1.5">
                  Email or Username
                </label>
                <input
                  type="text"
                  required
                  value={loginIdentifier}
                  onChange={(e) => setLoginIdentifier(e.target.value)}
                  placeholder="juan or juan@example.com"
                  className="w-full px-3.5 py-2.5 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-[#2D1B22]/80 mb-1.5">
                  Password
                </label>
                <input
                  type="password"
                  required
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full px-3.5 py-2.5 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
                />
              </div>

              <button
                type="submit"
                className="w-full py-2.5 px-4 bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] text-white font-medium text-sm rounded-xl shadow-sm transition-colors flex items-center justify-center gap-2"
              >
                <LogIn className="w-4 h-4" />
                <span>Log In</span>
              </button>
            </form>
          ) : (
            <form onSubmit={handleRegisterSubmit} className="space-y-3.5">
              <div>
                <label className="block text-xs font-medium text-[#2D1B22]/80 mb-1">
                  Full Name
                </label>
                <input
                  type="text"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="e.g. Sofia Navarro"
                  className="w-full px-3.5 py-2 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-[#2D1B22]/80 mb-1">
                    Username
                  </label>
                  <input
                    type="text"
                    required
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="sofia_n"
                    className="w-full px-3.5 py-2 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-[#2D1B22]/80 mb-1">
                    Email
                  </label>
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="sofia@mail.com"
                    className="w-full px-3.5 py-2 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-[#2D1B22]/80 mb-1">
                  Password (min 6 characters)
                </label>
                <input
                  type="password"
                  required
                  minLength={6}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full px-3.5 py-2 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-[#2D1B22]/80 mb-1">
                  Confirm Password
                </label>
                <input
                  type="password"
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full px-3.5 py-2 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
                />
              </div>

              <button
                type="submit"
                className="w-full mt-2 py-2.5 px-4 bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] text-white font-medium text-sm rounded-xl shadow-sm transition-colors flex items-center justify-center gap-2"
              >
                <UserPlus className="w-4 h-4" />
                <span>Create Account</span>
              </button>
            </form>
          )}

          {/* Quick Demo Pre-filled Accounts */}
          <div className="pt-4 border-t border-[#E1CCD4]/70">
            <p className="text-[11px] font-semibold text-[#2D1B22]/60 uppercase tracking-wider mb-2.5 text-center">
              Quick Test Accounts
            </p>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => handleQuickDemoLogin('user-juan')}
                className="p-2 text-center rounded-xl border border-[#E1CCD4] hover:border-[#69042A] bg-[#FAF7F8] hover:bg-white transition-all group"
              >
                <div className="text-xs font-semibold text-[#69042A] group-hover:underline">Juan</div>
                <div className="text-[10px] text-[#2D1B22]/50">Family Creator</div>
              </button>
              <button
                type="button"
                onClick={() => handleQuickDemoLogin('user-maria')}
                className="p-2 text-center rounded-xl border border-[#E1CCD4] hover:border-[#69042A] bg-[#FAF7F8] hover:bg-white transition-all group"
              >
                <div className="text-xs font-semibold text-[#69042A] group-hover:underline">María</div>
                <div className="text-[10px] text-[#2D1B22]/50">Member</div>
              </button>
              <button
                type="button"
                onClick={() => handleQuickDemoLogin('user-carlos')}
                className="p-2 text-center rounded-xl border border-[#E1CCD4] hover:border-[#69042A] bg-[#FAF7F8] hover:bg-white transition-all group"
              >
                <div className="text-xs font-semibold text-[#69042A] group-hover:underline">Carlos</div>
                <div className="text-[10px] text-[#2D1B22]/50">Has Invite</div>
              </button>
            </div>
          </div>
        </div>

        {/* Quiet footer reassurance */}
        <p className="text-center text-xs text-[#2D1B22]/50">
          Manual & private debt ledger. No bank connection or cards required.
        </p>
      </div>
    </div>
  );
};
