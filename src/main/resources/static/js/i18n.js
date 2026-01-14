/**
 * i18n - Client-side internationalization module
 * Supports: Korean (ko), English (en)
 */

const i18n = (() => {
    // Translation data
    const translations = {
        ko: {
            // Common
            'app.name': 'CITY BUILDER',
            'common.loading': '로딩 중...',
            'common.save': '저장',
            'common.cancel': '취소',
            'common.confirm': '확인',
            'common.delete': '삭제',
            'common.home': '홈',
            'common.mypage': '마이페이지',
            'common.login': '로그인',
            'common.logout': '로그아웃',
            'common.signup': '회원가입',

            // City page - Top bar
            'city.title': '{username}의 도시',
            'city.yours': '의 도시',

            // City page - HUD
            'hud.budget': '예산',
            'hud.tax': '세금',
            'hud.perHour': '/시간',
            'hud.offlineEarnings': '오프라인 수익',
            'hud.collect': '수령하기',
            'hud.cityStats': '도시 현황',
            'hud.population': '인구',
            'hud.happiness': '행복도',
            'hud.power': '전력',
            'hud.traffic': '교통',
            'hud.crime': '범죄율',
            'hud.daily': '일일',
            'hud.ap': '행동력',
            'hud.loginStreak': '연속 출석',
            'hud.days': '일',
            'hud.loginReward': '출석 보상!',
            'hud.claim': '받기',

            // City page - Toolbar
            'toolbar.camera': '카메라',
            'toolbar.view': '보기',
            'toolbar.road': '도로',
            'toolbar.roads': '도로',
            'toolbar.residential': '주거',
            'toolbar.commercial': '상업',
            'toolbar.industrial': '공업',
            'toolbar.delete': '삭제',
            'toolbar.zones': '구역',
            'toolbar.build': '건설',
            'toolbar.power': '전력',
            'toolbar.services': '서비스',
            'toolbar.save': '저장',
            'toolbar.night': '밤',
            'toolbar.day': '낮',

            // City page - Game time
            'game.day': 'Day {day}',
            'game.speed1': '1배속',
            'game.speed2': '2배속',
            'game.speed3': '3배속',

            // City page - Hints
            'hint.camera': '카메라 모드',
            'hint.road': '도로 건설 모드 - 드래그하여 도로 건설',
            'hint.residential': '주거 구역 지정 모드',
            'hint.commercial': '상업 구역 지정 모드',
            'hint.industrial': '공업 구역 지정 모드',
            'hint.delete': '삭제 모드 - 클릭하여 건물 삭제',
            'hint.viewOnly': '{username}의 도시 (보기 전용)',

            // Toast messages
            'toast.insufficientFunds': '자금이 부족해요',
            'toast.insufficientFundsDesc': '{item}에 {cost}원이 필요합니다. 현재 {current}원 보유 중이에요.',
            'toast.taxCollected': '세금 수집 완료!',
            'toast.taxCollectedDesc': '자금이 입금되었습니다.',
            'toast.rewardClaimed': '보상 수령 완료!',
            'toast.rewardClaimedDesc': '출석 보상이 입금되었습니다.',
            'toast.saved': '저장 완료',
            'toast.savedDesc': '도시가 저장되었습니다.',
            'toast.saving': '저장 중...',
            'toast.comingSoon': '준비 중',
            'toast.comingSoonDesc': '이 기능은 곧 추가될 예정입니다!',
            'toast.saveError': '저장 실패',
            'toast.saveErrorDesc': '저장 중 오류가 발생했습니다.',
            'toast.loadError': '로드 실패',
            'toast.loadErrorDesc': '도시를 불러오는데 실패했습니다.',
            'toast.cannotModify': '수정 불가',
            'toast.cannotModifyDesc': '다른 사용자의 도시는 수정할 수 없습니다.',
            'toast.powerShortage': '전력 부족!',
            'toast.powerShortageDesc': '발전소를 더 건설하세요.',

            // Buildings
            'building.road': '도로',
            'building.powerPlant': '발전소',
            'building.policeStation': '경찰서',
            'building.fireStation': '소방서',
            'building.park': '공원',
            'building.school': '학교',
            'building.hospital': '병원',
            'building.residentialLow': '하류층 주거',
            'building.residentialMid': '중류층 주거',
            'building.residentialHigh': '상류층 주거',
            'building.commercial': '상업 시설',
            'building.industrial': '공업 시설',

            // Home page
            'home.tagline': '꿈의 도시를 건설하세요',
            'home.myCity': '내 도시',
            'home.myCities': '내 도시 목록',
            'home.online': '온라인',
            'home.enterCity': '내 도시 입장',
            'home.welcomeMayor': '환영합니다, 시장님!',
            'home.description': '나만의 도시를 건설하고 관리하세요. 도로를 건설하고, 인프라를 개발하고, 도시가 성장하는 것을 지켜보세요!',
            'home.startBuilding': '건설 시작',
            'home.alreadyHaveCity': '이미 도시가 있으신가요? 로그인',
            'home.logoutSuccess': '로그아웃 되었습니다. 다음에 또 만나요, 시장님!',
            'home.cityCount': '도시 개수:',
            'home.createNewCity': '새 도시 만들기',
            'home.newCityName': '새 도시 이름',
            'home.create': '만들기',
            'home.enter': '입장',
            'home.delete': '삭제',
            'home.noCities': '도시가 없습니다. 첫 번째 도시를 만들어보세요!',

            // Login page
            'login.title': '로그인',
            'login.welcomeBack': '다시 오신 것을 환영합니다',
            'login.subtitle': '도시 건설을 계속하려면 로그인하세요',
            'login.username': '아이디',
            'login.usernamePlaceholder': '아이디를 입력하세요',
            'login.password': '비밀번호',
            'login.passwordPlaceholder': '비밀번호를 입력하세요',
            'login.submit': '로그인',
            'login.rememberMe': '로그인 유지',
            'login.noAccount': '계정이 없으신가요?',
            'login.createAccount': '회원가입',
            'login.error': '아이디 또는 비밀번호가 올바르지 않습니다.',
            'login.sessionExpired': '다른 기기에서 로그인하여 현재 세션이 종료되었습니다.',
            'login.alreadyLoggedIn': '이 계정은 이미 다른 곳에서 로그인 중입니다. 먼저 로그아웃 해주세요.',
            'login.emailVerified': '이메일 인증 완료! 이제 로그인할 수 있습니다.',
            'common.or': '또는',
            'common.backToHome': '← 홈으로 돌아가기',

            // Signup page
            'signup.title': '회원가입',
            'signup.createAccount': '계정 만들기',
            'signup.subtitle': '지금 가입하고 도시 건설을 시작하세요',
            'signup.username': '아이디',
            'signup.usernamePlaceholder': '아이디를 선택하세요',
            'signup.usernameHint': '4-20자, 영문과 숫자만 가능',
            'signup.email': '이메일',
            'signup.emailPlaceholder': '이메일을 입력하세요',
            'signup.emailHint': '인증 이메일을 보내드립니다',
            'signup.password': '비밀번호',
            'signup.passwordPlaceholder': '비밀번호를 만드세요',
            'signup.passwordHint': '최소 8자 이상',
            'signup.confirmPassword': '비밀번호 확인',
            'signup.submit': '계정 만들기',
            'signup.featuresTitle': '가입 혜택',
            'signup.feature1': '고유 URL의 나만의 도시',
            'signup.feature2': '도로와 인프라 건설',
            'signup.feature3': '다른 사람과 도시 공유',
            'signup.hasAccount': '이미 계정이 있으신가요?',
            'signup.login': '로그인',

            // My page
            'mypage.title': '마이페이지',
            'mypage.myCity': '내 도시',
            'mypage.settings': '설정',
            'mypage.changePassword': '비밀번호 변경',
            'mypage.currentPassword': '현재 비밀번호',
            'mypage.newPassword': '새 비밀번호',
            'mypage.confirmNewPassword': '새 비밀번호 확인',
        },

        en: {
            // Common
            'app.name': 'CITY BUILDER',
            'common.loading': 'Loading...',
            'common.save': 'Save',
            'common.cancel': 'Cancel',
            'common.confirm': 'Confirm',
            'common.delete': 'Delete',
            'common.home': 'Home',
            'common.mypage': 'My Page',
            'common.login': 'Login',
            'common.logout': 'Logout',
            'common.signup': 'Sign Up',

            // City page - Top bar
            'city.title': "{username}'s City",
            'city.yours': "'s City",

            // City page - HUD
            'hud.budget': 'Budget',
            'hud.tax': 'Tax',
            'hud.perHour': '/hr',
            'hud.offlineEarnings': 'Offline Earnings',
            'hud.collect': 'Collect',
            'hud.cityStats': 'City Stats',
            'hud.population': 'Population',
            'hud.happiness': 'Happiness',
            'hud.power': 'Power',
            'hud.traffic': 'Traffic',
            'hud.crime': 'Crime',
            'hud.daily': 'Daily',
            'hud.ap': 'AP',
            'hud.loginStreak': 'Login Streak',
            'hud.days': 'days',
            'hud.loginReward': 'Login Reward!',
            'hud.claim': 'Claim',

            // City page - Toolbar
            'toolbar.camera': 'Camera',
            'toolbar.view': 'View',
            'toolbar.road': 'Road',
            'toolbar.roads': 'Roads',
            'toolbar.residential': 'Residential',
            'toolbar.commercial': 'Commercial',
            'toolbar.industrial': 'Industrial',
            'toolbar.delete': 'Delete',
            'toolbar.zones': 'Zones',
            'toolbar.build': 'Build',
            'toolbar.power': 'Power',
            'toolbar.services': 'Services',
            'toolbar.save': 'Save',
            'toolbar.night': 'Night',
            'toolbar.day': 'Day',

            // City page - Game time
            'game.day': 'Day {day}',
            'game.speed1': '1x Speed',
            'game.speed2': '2x Speed',
            'game.speed3': '3x Speed',

            // City page - Hints
            'hint.camera': 'Camera Mode',
            'hint.road': 'Road Mode - Drag to build roads',
            'hint.residential': 'Residential Zone Mode',
            'hint.commercial': 'Commercial Zone Mode',
            'hint.industrial': 'Industrial Zone Mode',
            'hint.delete': 'Delete Mode - Click to remove',
            'hint.viewOnly': "{username}'s City (View Only)",

            // Toast messages
            'toast.insufficientFunds': 'Insufficient Funds',
            'toast.insufficientFundsDesc': '{item} costs {cost}. You have {current}.',
            'toast.taxCollected': 'Tax Collected!',
            'toast.taxCollectedDesc': 'Funds have been deposited.',
            'toast.rewardClaimed': 'Reward Claimed!',
            'toast.rewardClaimedDesc': 'Login reward has been deposited.',
            'toast.saved': 'Saved',
            'toast.savedDesc': 'City has been saved.',
            'toast.saving': 'Saving...',
            'toast.comingSoon': 'Coming Soon',
            'toast.comingSoonDesc': 'This feature will be available soon!',
            'toast.saveError': 'Save Failed',
            'toast.saveErrorDesc': 'An error occurred while saving.',
            'toast.loadError': 'Load Failed',
            'toast.loadErrorDesc': 'Failed to load the city.',
            'toast.cannotModify': 'Cannot Modify',
            'toast.cannotModifyDesc': "You cannot modify other users' cities.",
            'toast.powerShortage': 'Power Shortage!',
            'toast.powerShortageDesc': 'Build more power plants.',

            // Buildings
            'building.road': 'Road',
            'building.powerPlant': 'Power Plant',
            'building.policeStation': 'Police Station',
            'building.fireStation': 'Fire Station',
            'building.park': 'Park',
            'building.school': 'School',
            'building.hospital': 'Hospital',
            'building.residentialLow': 'Low-income Housing',
            'building.residentialMid': 'Mid-income Housing',
            'building.residentialHigh': 'High-income Housing',
            'building.commercial': 'Commercial',
            'building.industrial': 'Industrial',

            // Home page
            'home.tagline': 'Build Your Dream City',
            'home.myCity': 'My City',
            'home.myCities': 'My Cities',
            'home.online': 'Online',
            'home.enterCity': 'Enter My City',
            'home.welcomeMayor': 'Welcome, Mayor!',
            'home.description': 'Build and manage your own city. Create roads, develop infrastructure, and watch your city grow!',
            'home.startBuilding': 'Start Building',
            'home.alreadyHaveCity': 'Already have a city? Login',
            'home.logoutSuccess': 'Successfully logged out. See you next time, Mayor!',
            'home.cityCount': 'Cities:',
            'home.createNewCity': 'Create New City',
            'home.newCityName': 'New city name',
            'home.create': 'Create',
            'home.enter': 'Enter',
            'home.delete': 'Delete',
            'home.noCities': 'No cities yet. Create your first city!',

            // Login page
            'login.title': 'Login',
            'login.welcomeBack': 'Welcome Back',
            'login.subtitle': 'Sign in to continue building your city',
            'login.username': 'Username',
            'login.usernamePlaceholder': 'Enter your username',
            'login.password': 'Password',
            'login.passwordPlaceholder': 'Enter your password',
            'login.submit': 'Sign In',
            'login.rememberMe': 'Keep me logged in',
            'login.noAccount': "Don't have an account?",
            'login.createAccount': 'Sign Up',
            'login.error': 'Invalid username or password.',
            'login.sessionExpired': 'Your session has been terminated because you logged in from another device.',
            'login.alreadyLoggedIn': 'This account is already logged in elsewhere. Please log out first.',
            'login.emailVerified': 'Email verified! You can now login.',
            'common.or': 'OR',
            'common.backToHome': '← Back to Home',

            // Signup page
            'signup.title': 'Sign Up',
            'signup.createAccount': 'Create Account',
            'signup.subtitle': 'Join and start building your city today',
            'signup.username': 'Username',
            'signup.usernamePlaceholder': 'Choose a username',
            'signup.usernameHint': '4-20 characters, letters and numbers only',
            'signup.email': 'Email',
            'signup.emailPlaceholder': 'Enter your email',
            'signup.emailHint': "We'll send a verification email",
            'signup.password': 'Password',
            'signup.passwordPlaceholder': 'Create a password',
            'signup.passwordHint': 'Minimum 8 characters',
            'signup.confirmPassword': 'Confirm Password',
            'signup.submit': 'Create Account',
            'signup.featuresTitle': "What you'll get",
            'signup.feature1': 'Your own city with unique URL',
            'signup.feature2': 'Build roads and infrastructure',
            'signup.feature3': 'Share your city with others',
            'signup.hasAccount': 'Already have an account?',
            'signup.login': 'Sign in',

            // My page
            'mypage.title': 'My Page',
            'mypage.myCity': 'My City',
            'mypage.settings': 'Settings',
            'mypage.changePassword': 'Change Password',
            'mypage.currentPassword': 'Current Password',
            'mypage.newPassword': 'New Password',
            'mypage.confirmNewPassword': 'Confirm New Password',
        }
    };

    // Current language
    let currentLang = 'en';

    /**
     * Detect browser language and set accordingly
     */
    function detectLanguage() {
        const browserLang = navigator.language || navigator.userLanguage;
        if (browserLang.startsWith('ko')) {
            currentLang = 'ko';
        } else {
            currentLang = 'en';
        }
        // Check localStorage for user preference
        const savedLang = localStorage.getItem('i18n-lang');
        if (savedLang && translations[savedLang]) {
            currentLang = savedLang;
        }
        return currentLang;
    }

    /**
     * Get current language
     */
    function getLang() {
        return currentLang;
    }

    /**
     * Set language
     */
    function setLang(lang) {
        if (translations[lang]) {
            currentLang = lang;
            localStorage.setItem('i18n-lang', lang);
            applyTranslations();
            return true;
        }
        return false;
    }

    /**
     * Get translation for a key
     * @param {string} key - Translation key
     * @param {object} params - Parameters for interpolation
     */
    function t(key, params = {}) {
        const langData = translations[currentLang] || translations['en'];
        let text = langData[key] || translations['en'][key] || key;

        // Replace parameters {param}
        Object.keys(params).forEach(param => {
            text = text.replace(new RegExp(`\\{${param}\\}`, 'g'), params[param]);
        });

        return text;
    }

    /**
     * Apply translations to all elements with data-i18n attribute
     */
    function applyTranslations() {
        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key = el.getAttribute('data-i18n');
            const params = el.getAttribute('data-i18n-params');

            let parsedParams = {};
            if (params) {
                try {
                    parsedParams = JSON.parse(params);
                } catch (e) {
                    console.warn('Invalid i18n params:', params);
                }
            }

            el.textContent = t(key, parsedParams);
        });

        // Apply to placeholders
        document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
            const key = el.getAttribute('data-i18n-placeholder');
            el.placeholder = t(key);
        });

        // Apply to titles
        document.querySelectorAll('[data-i18n-title]').forEach(el => {
            const key = el.getAttribute('data-i18n-title');
            el.title = t(key);
        });

        // Update html lang attribute
        document.documentElement.lang = currentLang;
    }

    /**
     * Initialize i18n
     */
    function init() {
        detectLanguage();
        applyTranslations();
    }

    /**
     * Format money based on locale
     */
    function formatMoney(amount) {
        if (currentLang === 'ko') {
            return Math.floor(amount).toLocaleString('ko-KR');
        }
        return Math.floor(amount).toLocaleString('en-US');
    }

    /**
     * Get currency symbol
     */
    function getCurrency() {
        return currentLang === 'ko' ? '₩' : '$';
    }

    // Auto-init when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Public API
    return {
        t,
        getLang,
        setLang,
        detectLanguage,
        applyTranslations,
        formatMoney,
        getCurrency,
        init
    };
})();

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = i18n;
}
