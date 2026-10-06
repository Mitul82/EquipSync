import '@/index.css';

import { Toaster } from 'react-hot-toast';
import { RouterProvider, createBrowserRouter, createRoutesFromElements, Route, Outlet } from 'react-router-dom';

import EmployeeLayout from '@/components/EmployeeComponents/layout';

import LoginPage from '@/pages/LoginPage';
import SignupPage from '@/pages/SignupPage';
import AssetsPage from '@/pages/common/AssetsPage';
import MyRequestsPage from '@/pages/common/MyRequestsPage';
import DashboardPage from '@/pages/EmployeePages/DashboardPage';
import CreateRequestPage from '@/pages/common/RaiseRequestPage';
import AvailableAssetsPage from '@/pages/common/AvailableAssetsPage';

const router = createBrowserRouter(createRoutesFromElements(
	<>
		<Route path='/' element={<LoginPage/>}/>
		<Route path='/signup' element={ <SignupPage/> }/>
		<Route path='/dashboard' element={ <EmployeeLayout/> }>	// TODO: add the protected route element for Dashboard route
			<Route index element={ <DashboardPage/> }/>
			<Route path='assets' element={ <Outlet/> }>
				<Route index element={ <AssetsPage/> }/>
				<Route path='available' element={ <AvailableAssetsPage/> }/>
			</Route>
			<Route path='requests' element={ <Outlet/> }>
				<Route index element={ <MyRequestsPage/> }/>
				<Route path='create' element={ <CreateRequestPage/> }/>
			</Route>
		</Route>
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
          	duration: 5000,
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