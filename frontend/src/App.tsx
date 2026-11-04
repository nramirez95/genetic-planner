import { Route, Routes } from 'react-router';

import AppLayout from './components/layout/AppLayout';

import DashboardPage from './pages/DashboardPage';
import WizardPage from './pages/WizardPage';
import ResultsPage from './pages/ResultsPage';
import NotFoundPage from './pages/NotFoundPage';
import PlanningDetailPage from './pages/PlanningDetailPage';
import TemplateSelectionPage from './pages/TemplateSelectionPage';

export default function App() {
    return (
        <Routes>
            <Route element={<AppLayout />}>
                <Route path="/" element={<DashboardPage />} />
                <Route
                    path="/planning/new"
                    element={<TemplateSelectionPage />}
                />
                <Route
                    path="/planning/new/academic"
                    element={<WizardPage />}
                />
                <Route
                    path="/planning/:id"
                    element={<PlanningDetailPage />}
                />
                <Route
                    path="/planning/:id/results"
                    element={<ResultsPage />}
                />
                <Route path="*" element={<NotFoundPage />} />
            </Route>
        </Routes>
    );
}