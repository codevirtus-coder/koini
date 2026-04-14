import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { Toaster } from 'sonner';
import { useAuth } from './hooks/useAuth';

import LandingPage from './pages/landing/LandingPage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import SetupPinPage from './pages/auth/SetupPinPage';
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage';

import PassengerLayout from './pages/passenger/PassengerLayout';
import PassengerDashboard from './pages/passenger/DashboardPage';
import PayFarePage from './pages/passenger/PayFarePage';
import PassengerTransactions from './pages/passenger/TransactionsPage';
import TransferPage from './pages/passenger/TransferPage';
import ProfilePage from './pages/passenger/ProfilePage';
import PassengerTopUpPage from './pages/passenger/TopUpPage';

import ConductorLayout from './pages/conductor/ConductorLayout';
import ConductorDashboard from './pages/conductor/DashboardPage';
import RedeemCodePage from './pages/conductor/RedeemCodePage';
import RequestPaymentPage from './pages/conductor/RequestPaymentPage';
import ConductorTransactions from './pages/conductor/TransactionsPage';
import ShiftReportPage from './pages/conductor/ShiftReportPage';
import MerchantOnboardingPage from './pages/conductor/MerchantOnboardingPage';
import RequestsPage from './pages/conductor/RequestsPage';

import AgentLayout from './pages/agent/AgentLayout';
import AgentDashboard from './pages/agent/DashboardPage';
import TopUpPage from './pages/agent/TopUpPage';
import WithdrawalPage from './pages/agent/WithdrawalPage';
import AgentTransactions from './pages/agent/TransactionsPage';
import DailySummaryPage from './pages/agent/DailySummaryPage';

import AdminLayout from './pages/admin/AdminLayout';
import AdminDashboard from './pages/admin/DashboardPage';
import UsersPage from './pages/admin/users/UsersPage';
import UserDetailPage from './pages/admin/users/UserDetailPage';
import CreateUserPage from './pages/admin/users/CreateUserPage';
import PendingMerchantsPage from './pages/admin/merchants/PendingMerchantsPage';
import AdminTransactions from './pages/admin/transactions/TransactionsPage';
import RoutesPage from './pages/admin/routes/RoutesPage';
import CreateRoutePage from './pages/admin/routes/CreateRoutePage';
import AgentsPage from './pages/admin/agents/AgentsPage';
import AuditLogPage from './pages/admin/audit/AuditLogPage';
import ReconciliationPage from './pages/admin/reconciliation/ReconciliationPage';
import AdminIntegrationsPage from './pages/admin/integrations/IntegrationsPage';
import PesepayReturnPage from './pages/pesepay/PesepayReturnPage';

import NotFoundPage from './pages/NotFoundPage';
import { PortalGate } from './components/layout/PortalGate';
import { KoiniLogo } from './components/shared/KoiniLogo';

function AuthRoute({ children }: { children: React.ReactNode }): JSX.Element {
  const { state, portalPath } = useAuth();
  if (state.isLoading) {
    return (
      <div className="min-h-screen bg-surface-bg flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <KoiniLogo size={48} />
          <div className="w-8 h-8 border-2 border-surface-border border-t-primary-500 rounded-full animate-spin" />
        </div>
      </div>
    );
  }
  if (state.isAuthenticated) return <Navigate to={portalPath} replace />;
  return <>{children}</>;
}

function PrefixRedirect({ from, to }: { from: string; to: string }): JSX.Element {
  const location = useLocation();
  const path = location.pathname.startsWith(from) ? `${to}${location.pathname.slice(from.length)}` : to;
  return <Navigate to={`${path}${location.search}${location.hash}`} replace />;
}

export default function App(): JSX.Element {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/api/v1/webhooks/pesepay/return" element={<PesepayReturnPage />} />
        <Route path="/" element={<LandingPage />} />

        <Route path="/login" element={<AuthRoute><LoginPage /></AuthRoute>} />
        <Route path="/register" element={<AuthRoute><RegisterPage /></AuthRoute>} />
        <Route path="/forgot-password" element={<AuthRoute><ForgotPasswordPage /></AuthRoute>} />

        <Route
          path="/setup-pin"
          element={
            <PortalGate allowedRoles={['PASSENGER', 'CLIENT', 'CONDUCTOR', 'MERCHANT', 'AGENT', 'ADMIN', 'FLEET_OWNER']}>
              <SetupPinPage />
            </PortalGate>
          }
        />

        <Route
          path="/client"
          element={
            <PortalGate allowedRoles={['PASSENGER', 'CLIENT']}>
              <PassengerLayout />
            </PortalGate>
          }
        >
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<PassengerDashboard />} />
          <Route path="pay" element={<PayFarePage />} />
          <Route path="topup" element={<PassengerTopUpPage />} />
          <Route path="transactions" element={<PassengerTransactions />} />
          <Route path="transfer" element={<TransferPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>

        <Route path="/passenger/*" element={<PrefixRedirect from="/passenger" to="/client" />} />

        <Route
          path="/merchant"
          element={
            <PortalGate allowedRoles={['CONDUCTOR', 'MERCHANT']}>
              <ConductorLayout />
            </PortalGate>
          }
        >
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<ConductorDashboard />} />
          <Route path="onboarding" element={<MerchantOnboardingPage />} />
          <Route path="redeem" element={<RedeemCodePage />} />
          <Route path="request" element={<RequestPaymentPage />} />
          <Route path="requests" element={<RequestsPage />} />
          <Route path="transactions" element={<ConductorTransactions />} />
          <Route path="shift" element={<ShiftReportPage />} />
        </Route>

        <Route path="/conductor/*" element={<PrefixRedirect from="/conductor" to="/merchant" />} />

        <Route
          path="/agent"
          element={
            <PortalGate allowedRoles={['AGENT']}>
              <AgentLayout />
            </PortalGate>
          }
        >
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<AgentDashboard />} />
          <Route path="topup" element={<TopUpPage />} />
          <Route path="withdrawal" element={<WithdrawalPage />} />
          <Route path="transactions" element={<AgentTransactions />} />
          <Route path="summary" element={<DailySummaryPage />} />
        </Route>

        <Route
          path="/admin"
          element={
            <PortalGate allowedRoles={['ADMIN']}>
              <AdminLayout />
            </PortalGate>
          }
        >
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<AdminDashboard />} />
          <Route path="users" element={<UsersPage />} />
          <Route path="users/:id" element={<UserDetailPage />} />
          <Route path="users/create" element={<CreateUserPage />} />
          <Route path="merchants" element={<PendingMerchantsPage />} />
          <Route path="transactions" element={<AdminTransactions />} />
          <Route path="routes" element={<RoutesPage />} />
          <Route path="routes/create" element={<CreateRoutePage />} />
          <Route path="agents" element={<AgentsPage />} />
          <Route path="integrations" element={<AdminIntegrationsPage />} />
          <Route path="audit" element={<AuditLogPage />} />
          <Route path="reconciliation" element={<ReconciliationPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>

      <Toaster
        theme="dark"
        position="top-right"
        toastOptions={{
          classNames: {
            toast: 'bg-surface-card border border-surface-border text-text-primary',
            success: 'border-success-500/30',
            error: 'border-danger-500/30',
            warning: 'border-accent-500/30',
          },
        }}
      />
    </BrowserRouter>
  );
}
