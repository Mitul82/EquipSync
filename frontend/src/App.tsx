import '@/index.css';

import { Toaster } from 'react-hot-toast';
import { RouterProvider, createBrowserRouter, createRoutesFromElements, Route } from 'react-router-dom';

import LoginPage from '@/pages/LoginPage';

const router = createBrowserRouter(createRoutesFromElements(
	<>
		<Route path='/' element={<LoginPage/>}/>
	</>
));

function App() {
	const toastOption = {
        style: {
        	background: '#ffffff',
        	color: '#AF000F',
        	border: '1px solid #AF000F',
        },
        success: {
        	duration: 5000,
        	iconTheme: {
        		primary: '#09af00',
        		secondary: '#ffffff',
        	},
        },
        error: {
          	duration: 6000,
          	iconTheme: {
            	primary: '#AF000F',
            	secondary: '#ffffff',
          	},
        },
	}

	return (
		<>
			<Toaster toastOptions={ toastOption } position='bottom-center'/>
			<RouterProvider router={ router }/>
		</>
	)
}

export default App;