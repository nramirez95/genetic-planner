
import {
    Box,
    Tab,
    Tabs,
    Typography,
} from '@mui/material';

import {
    useFormContext,
    useWatch,
} from 'react-hook-form';

import type { AcademicWizardValues } from '../types/wizard';

import ClassroomEditor from '../../scheduling/components/ClassroomEditor';
import TeachingPeriodEditor from '../../scheduling/components/TeachingPeriodEditor';

import styles from '../components/PlanningWizard.module.css';

export type SchedulingTab = 'periods' | 'classrooms';

interface TimeSlotsLocationsStepProps {
    activeTab: SchedulingTab;
    onTabChange: (tab: SchedulingTab) => void;
    invalidPeriodIndex: number | null;
    validationRequestId: number;
}

export default function TimeSlotsLocationsStep({
                                                   activeTab,
                                                   onTabChange,
                                                   invalidPeriodIndex,
                                                   validationRequestId,
                                               }: TimeSlotsLocationsStepProps) {
    const { control } = useFormContext<AcademicWizardValues>();

    const periods =
        useWatch({ control, name: 'teachingPeriods' }) ?? [];

    const classrooms =
        useWatch({ control, name: 'classrooms' }) ?? [];

    return (
        <Box className={styles.stepContent}>
            <Box>
                <Typography variant="h5" component="h2">
                    Time Slots & Locations
                </Typography>

                <Typography color="text.secondary">
                    Configure teaching periods and classrooms for
                    your academic planning.
                </Typography>
            </Box>

            <Tabs
                value={activeTab}
                onChange={(_, value: SchedulingTab) =>
                    onTabChange(value)
                }
                variant="scrollable"
                scrollButtons="auto"
                aria-label="Scheduling configuration"
            >
                <Tab
                    value="periods"
                    label={`Teaching periods (${periods.length})`}
                />

                <Tab
                    value="classrooms"
                    label={`Classrooms (${classrooms.length})`}
                />
            </Tabs>

            <Box
                role="tabpanel"
                hidden={activeTab !== 'periods'}
                id="scheduling-panel-periods"
                aria-label="Teaching periods"
            >
                {activeTab === 'periods' && (
                    <TeachingPeriodEditor
                        key={validationRequestId}
                        invalidPeriodIndex={invalidPeriodIndex}
                    />
                )}
            </Box>

            <Box
                role="tabpanel"
                hidden={activeTab !== 'classrooms'}
                id="scheduling-panel-classrooms"
                aria-label="Classrooms"
            >
                {activeTab === 'classrooms' && (
                    <ClassroomEditor />
                )}
            </Box>
        </Box>
    );
}
