import ReactDOM from 'react-dom/client';

import App from '@/App.tsx';
import Providers from '@/utils/providers';

const root = ReactDOM.createRoot(document.getElementById('root')!);

root.render(
	<Providers>
		<App/>
	</Providers>
);