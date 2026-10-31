import {
  Box,
  Container,
  Typography,
} from '@mui/material';

export default function App() {
  return (
      <Container maxWidth="lg">
        <Box sx={{ py: 6 }}>
          <Typography variant="h4" component="h1" gutterBottom>
            Genetic Planner
          </Typography>

          <Typography color="text.secondary">
            Academic and work shift scheduling
          </Typography>
        </Box>
      </Container>
  );
}