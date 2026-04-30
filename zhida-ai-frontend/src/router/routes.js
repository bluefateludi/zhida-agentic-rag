export const appRoutes = [
  {
    path: '/',
    name: 'LandingPage',
    component: () => import('../views/LandingPage.vue'),
    meta: {
      title: '智答 AI - 企业知识智能中枢',
      description: '面向软件工程课设答辩展示的企业知识库问答平台'
    }
  },
  {
    path: '/chat',
    name: 'KnowledgeBase',
    component: () => import('../views/KnowledgeBase.vue'),
    meta: {
      title: '聊天工作台 - 智答 AI',
      description: '保留聊天问答、来源引用和文档状态的工作台'
    }
  },
  {
    path: '/chat/documents',
    name: 'DocumentManager',
    component: () => import('../views/DocumentManager.vue'),
    meta: {
      title: '知识库管理 - 智答 AI',
      description: '管理知识库文档，上传、分类、删除文档'
    }
  },
  {
    path: '/pm',
    name: 'PmMode',
    component: () => import('../views/PmMode.vue'),
    meta: {
      title: '产品分析模式 - 智答 AI',
      description: '保留的产品分析模式，融合联网研究与知识库参考'
    }
  },
  {
    path: '/documents',
    redirect: '/chat/documents'
  }
]
