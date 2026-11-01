import { Route, Routes } from 'react-router';

import AppLayout from './components/layout/AppLayout';

import DashboardPage from './pages/DashboardPage';
import WizardPage from './pages/WizardPage';
import ResultsPage from './pages/ResultsPage';
import NotFoundPage from './pages/NotFoundPage';

export default function App() {
    return (
        <Routes>
            <Route element={<AppLayout />}>
                <Route index element={<DashboardPage />} />

                <Route
                    path="planning/new"
                    element={<WizardPage />}
                />

                <Route
                    path="planning/:id/results"
                    element={<ResultsPage />}
                />

                <Route path="*" element={<NotFoundPage />} />
            </Route>
        </Routes>
    );
}