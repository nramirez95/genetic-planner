import { useNavigate } from 'react-router';

import TemplateSelector from '../features/templates/components/TemplateSelector';

export default function TemplateSelectionPage() {
    const navigate = useNavigate();

    return (
        <TemplateSelector
            onContinue={(template) => {
                if (template === 'ACADEMIC') {
                    navigate('/planning/new/academic');
                }
            }}
        />
    );
}