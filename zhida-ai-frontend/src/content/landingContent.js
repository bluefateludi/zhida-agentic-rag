export const landingContent = {
  nav: [
    { label: '产品定位', href: '#positioning' },
    { label: '核心能力', href: '#capabilities' },
    { label: '演示流程', href: '#demo-flow' },
    { label: '可追溯机制', href: '#traceability' }
  ],
  hero: {
    eyebrow: 'Knowledge Intelligence Workspace',
    title: '面向软件工程课设答辩的企业知识智能中枢',
    description: '将知识接入、检索增强问答、来源引用与文档状态整合进同一套演示叙事，让系统能力更容易被看见、被解释、被验证。',
    primaryAction: {
      label: '开始体验',
      href: '/chat'
    },
    secondaryAction: {
      label: '查看演示流程',
      href: '#demo-flow'
    },
    highlights: [
      'RAG 检索增强问答',
      '回答附带可追溯来源',
      '文档状态辅助演示说明'
    ]
  },
  positioning: {
    label: '产品定位',
    title: '不是通用聊天壳子，而是一套可展示、可解释、可验证的知识问答系统',
    description: '系统面向企业知识库场景，将资料接入、向量检索、流式回答、证据引用和索引状态组合成完整闭环，适合在答辩中清晰呈现系统架构与核心价值。',
    metrics: [
      { label: '定位', value: '课程项目答辩展示' },
      { label: '核心路径', value: '接入 -> 检索 -> 回答 -> 追溯' },
      { label: '可信辅助', value: '来源追溯与状态说明' }
    ]
  },
  capabilities: [
    {
      title: '知识接入',
      description: '支持上传与管理课程资料、项目文档和业务材料，为后续索引与检索提供稳定知识底座。',
      detail: '文档管理页保留原有上传、筛选、删除与索引状态展示能力。'
    },
    {
      title: '智能问答',
      description: '围绕当前知识库进行流式生成与检索增强回答，突出“基于知识回答而非凭空生成”。',
      detail: '聊天工作台保留现有会话、多轮问答与来源卡片展示。'
    },
    {
      title: '状态与追溯',
      description: '展示文档就绪度、索引状态与来源证据，帮助在答辩过程中解释回答可信度。',
      detail: '聊天工作台直接呈现文档状态、推荐问题、来源卡片和流式回答。'
    }
  ],
  demoFlow: [
    {
      step: '01',
      title: '接入课程资料',
      description: '上传文档并建立知识库，说明系统如何获得领域知识。'
    },
    {
      step: '02',
      title: '完成索引准备',
      description: '展示文档状态与知识库就绪度，说明检索前置过程。'
    },
    {
      step: '03',
      title: '发起智能问答',
      description: '在聊天工作台中提问，观察流式回答与多轮上下文。'
    },
    {
      step: '04',
      title: '查看来源引用',
      description: '强调回答附带证据来源，体现可追溯与可信性。'
    },
    {
      step: '05',
      title: '说明文档状态',
      description: '结合文档状态栏说明哪些资料已经可用于稳定回答。'
    }
  ],
  traceability: {
    label: '可追溯机制',
    title: 'RAG、来源追溯与文档状态共同构成可信回答链路',
    description: '系统不只给答案，还给出答案从哪里来、当前知识库是否就绪，便于老师从工程实现与可信性两个角度理解系统价值。',
    points: [
      {
        title: 'RAG 检索增强',
        description: '先检索再生成，让回答建立在已接入知识之上。'
      },
      {
        title: '来源与证据引用',
        description: '回答附带来源卡片，强调“答案有据可查”。'
      },
      {
        title: '文档状态说明',
        description: '通过状态栏说明文档是否完成索引，避免把未就绪资料误认为可检索知识。'
      }
    ]
  }
}
