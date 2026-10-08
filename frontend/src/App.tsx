import '@/index.css';

import { Toaster } from 'react-hot-toast';
import { RouterProvider, createBrowserRouter, createRoutesFromElements, Route, Outlet } from 'react-router-dom';

import ProtectedRoute from '@/components/ProtectedRoute';

import Layout from '@/components/layout';

import LoginPage from '@/pages/LoginPage';
import SignupPage from '@/pages/SignupPage';
import AssetsPage from '@/pages/common/AssetsPage';
import UsersPage from '@/pages/AdminPages/UsersPage';
import RequestsPage from '@/pages/common/RequestsPage';
import MyRequestsPage from '@/pages/common/MyRequestsPage';
import ManageAssetsPage from '@/pages/common/ManageAssetsPage';
import DashboardPage from '@/pages/EmployeePages/DashboardPage';
import CreateRequestPage from '@/pages/common/RaiseRequestPage';
import AvailableAssetsPage from '@/pages/common/AvailableAssetsPage';

const router = createBrowserRouter(createRoutesFromElements(
	<>
		<Route path='/' element={ <LoginPage/> }/>
		<Route path='/signup' element={ <SignupPage/> }/>

		<Route path='/dashboard' element={ <ProtectedRoute allowedRoles={['Admin', 'Manager', 'Employee']}><Layout/></ProtectedRoute> }>
			<Route index element={ <DashboardPage/> }/>
			<Route path='assets/available' element={ <AvailableAssetsPage/> }/>
			<Route path='requests' element={ <Outlet/> }>
				<Route index element={ <MyRequestsPage/> }/>
				<Route path='create' element={ <CreateRequestPage/> }/>
			</Route>

			<Route element={ <ProtectedRoute allowedRoles={['Admin', 'Manager']}/> }>
				<Route path='assets' element={ <AssetsPage/> }/>
			</Route>
		</Route>

		<Route path='/manage' element={ <ProtectedRoute allowedRoles={['Admin', 'Manager']}><Layout/></ProtectedRoute> }>
			<Route path='requests' element={ <RequestsPage/> }/>
			<Route path='assets' element={ <ManageAssetsPage/> }/>
			<Route path='users' element={ <UsersPage/> }/>
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
	);
}

export default App;