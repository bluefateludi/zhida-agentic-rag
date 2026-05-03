<template>
  <div class="landing-page">
    <div class="landing-noise"></div>

    <header class="landing-header">
      <div class="brand-lockup">
        <span class="brand-mark">
          <img src="https://econgencode-1379443208.cos.ap-shanghai.myqcloud.com/zhidalogo.png" alt="智答 AI logo" />
        </span>
        <div>
          <span class="eyebrow">答辩知识工作台</span>
          <strong>智答 AI</strong>
        </div>
      </div>

      <nav class="landing-nav">
        <a
          v-for="item in landingContent.nav"
          :key="item.href"
          :href="item.href"
        >
          {{ item.label }}
        </a>
      </nav>

      <router-link class="cta-button" :to="landingContent.hero.primaryAction.href">
        {{ landingContent.hero.primaryAction.label }}
      </router-link>
    </header>

    <main class="landing-main">
      <section class="hero-section">
        <div class="hero-copy">
          <span class="eyebrow">{{ landingContent.hero.eyebrow }}</span>
          <h1>{{ landingContent.hero.title }}</h1>
          <p class="hero-description">
            {{ landingContent.hero.description }}
          </p>

          <div class="hero-actions">
            <router-link class="cta-button hero-primary" :to="landingContent.hero.primaryAction.href">
              {{ landingContent.hero.primaryAction.label }}
            </router-link>
            <router-link class="secondary-button" to="/report">
              研究报告模式
            </router-link>
            <router-link class="secondary-button" to="/evaluation">
              RAG 评测
            </router-link>
            <router-link class="secondary-button" to="/traces">
              Trace Dashboard
            </router-link>
            <router-link class="secondary-button" to="/pm">
              产品经理模式
            </router-link>
            <a class="secondary-button" :href="landingContent.hero.secondaryAction.href">
              {{ landingContent.hero.secondaryAction.label }}
            </a>
          </div>

          <div class="hero-highlights">
            <span
              v-for="item in landingContent.hero.highlights"
              :key="item"
              class="hero-pill"
            >
              {{ item }}
            </span>
          </div>
        </div>

        <div class="hero-visual panel-card">
          <div class="visual-header">
            <span class="eyebrow">System Storyboard</span>
            <span class="status-pill">课设答辩模式</span>
          </div>

          <div class="visual-stage">
            <article class="signal-card signal-card-primary">
              <span>Knowledge Intake</span>
              <strong>文档接入与索引准备</strong>
              <p>上传课程资料后建立知识底座，为后续检索增强问答提供上下文。</p>
            </article>

            <article class="signal-card">
              <span>Grounded QA</span>
              <strong>流式智能问答</strong>
              <p>围绕知识库进行多轮对话，保留现有聊天工作台和来源引用能力。</p>
            </article>

            <article class="signal-card signal-card-accent">
              <span>Research Report</span>
              <strong>研究与写作双 Agent</strong>
              <p>把问题改写、证据归纳、信息缺口和 Markdown 报告汇总为更适合展示的研究模式。</p>
            </article>

            <article class="signal-card signal-card-grid">
              <div>
                <span>Source Trace</span>
                <strong>可追溯证据</strong>
              </div>
              <div>
                <span>Status Signal</span>
                <strong>文档状态说明</strong>
              </div>
            </article>
          </div>

          <div class="visual-footnote">
            <span>接入</span>
            <span></span>
            <span>检索</span>
            <span></span>
            <span>回答</span>
            <span></span>
            <span>追溯</span>
          </div>
        </div>
      </section>

      <section id="positioning" class="content-section">
        <div class="section-heading">
          <span class="eyebrow">{{ landingContent.positioning.label }}</span>
          <h2>{{ landingContent.positioning.title }}</h2>
          <p>{{ landingContent.positioning.description }}</p>
        </div>

        <div class="positioning-grid">
          <article
            v-for="metric in landingContent.positioning.metrics"
            :key="metric.label"
            class="panel-card metric-card"
          >
            <span>{{ metric.label }}</span>
            <strong>{{ metric.value }}</strong>
          </article>
        </div>
      </section>

      <section id="capabilities" class="content-section">
        <div class="section-heading">
          <span class="eyebrow">核心能力</span>
          <h2>把知识接入、智能问答与来源追溯收束到同一条展示主线</h2>
          <p>首页只负责讲清楚系统价值，真正的聊天和知识库操作全部放进 `/chat` 工作台。</p>
        </div>

        <div class="capability-grid">
          <article
            v-for="item in landingContent.capabilities"
            :key="item.title"
            class="panel-card capability-card"
          >
            <span class="card-index">{{ item.title }}</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
            <small>{{ item.detail }}</small>
          </article>
        </div>
      </section>

      <section id="demo-flow" class="content-section">
        <div class="section-heading">
          <span class="eyebrow">演示流程</span>
          <h2>适合现场答辩的五步演示路径</h2>
          <p>从资料接入到来源追溯，整个流程可以直接映射系统架构与用户价值。</p>
        </div>

        <div class="timeline-grid">
          <article
            v-for="item in landingContent.demoFlow"
            :key="item.step"
            class="panel-card timeline-card"
          >
            <span class="timeline-step">{{ item.step }}</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </article>
        </div>
      </section>

      <section id="traceability" class="content-section traceability-section">
        <div class="section-heading">
          <span class="eyebrow">{{ landingContent.traceability.label }}</span>
          <h2>{{ landingContent.traceability.title }}</h2>
          <p>{{ landingContent.traceability.description }}</p>
        </div>

        <div class="traceability-layout">
          <div class="panel-card traceability-diagram">
            <div class="diagram-node">
              <span>01</span>
              <strong>Knowledge Retrieval</strong>
            </div>
            <div class="diagram-link"></div>
            <div class="diagram-node">
              <span>02</span>
              <strong>Source Attribution</strong>
            </div>
            <div class="diagram-link"></div>
            <div class="diagram-node">
              <span>03</span>
              <strong>Status Explanation</strong>
            </div>
          </div>

          <div class="traceability-points">
            <article
              v-for="item in landingContent.traceability.points"
              :key="item.title"
              class="panel-card traceability-card"
            >
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </article>
          </div>
        </div>
      </section>

      <section class="closing-section panel-card">
        <div>
          <span class="eyebrow">Workspace Entry</span>
          <h2>首页负责展示，工作台负责真实操作。</h2>
          <p>进入 `/chat` 后保留现有聊天、来源引用和知识库管理能力，不改 RAG 问答主链路，只收敛前端信息架构。</p>
        </div>
        <div class="closing-actions">
          <router-link class="secondary-button" to="/report">
            进入研究报告模式
          </router-link>
          <router-link class="secondary-button" to="/evaluation">
            查看 RAG 评测
          </router-link>
          <router-link class="secondary-button" to="/traces">
            查看 Trace Dashboard
          </router-link>
          <router-link class="secondary-button" to="/pm">
            进入产品分析模式
          </router-link>
          <router-link class="cta-button hero-primary" to="/chat">
            进入聊天工作台
          </router-link>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { landingContent } from '../content/landingContent'
</script>

<style scoped>
.landing-page {
  position: relative;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  background:
    linear-gradient(135deg, rgba(88, 166, 173, 0.08), transparent 34%),
    linear-gradient(180deg, #071014 0%, #080b0d 32%, #050607 100%);
}

.landing-noise {
  position: fixed;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(rgba(218, 232, 238, 0.022) 1px, transparent 1px),
    linear-gradient(90deg, rgba(218, 232, 238, 0.022) 1px, transparent 1px);
  background-size: 120px 120px;
  opacity: 0.14;
}

.landing-header,
.landing-main {
  position: relative;
  z-index: 1;
  width: min(100%, 1240px);
  margin: 0 auto;
}

.landing-header {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 24px;
  padding: 24px 24px 0;
}

.brand-lockup {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  background: #ffffff;
  display: block;
  overflow: hidden;
  box-shadow:
    inset 0 0 0 1px rgba(229, 238, 245, 0.42),
    0 14px 28px rgba(31, 91, 98, 0.22);
}

.brand-mark img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transform: scale(1.78);
}

.brand-lockup strong {
  display: block;
  margin-top: 4px;
  font-size: 16px;
  font-weight: 510;
  color: var(--text-primary);
}

.landing-nav {
  display: flex;
  justify-content: center;
  gap: 18px;
  flex-wrap: wrap;
}

.landing-nav a,
.secondary-button {
  min-height: 38px;
  padding: 0 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.02);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 510;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.landing-nav a:hover,
.secondary-button:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.04);
  border-color: var(--border-default);
}

.cta-button {
  min-height: 42px;
  padding: 0 16px;
  border-radius: var(--radius-sm);
  background:
    linear-gradient(180deg, rgba(88, 166, 173, 0.26), rgba(47, 125, 134, 0.24)),
    var(--accent-strong);
  color: #f7fbfb;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  border: 1px solid rgba(126, 209, 216, 0.32);
  box-shadow:
    inset 0 1px 0 rgba(229, 238, 245, 0.16),
    0 14px 30px rgba(31, 91, 98, 0.24);
  transition: transform 0.2s ease, filter 0.2s ease;
}

.cta-button:hover {
  transform: translateY(-1px);
  filter: brightness(1.05);
}

.landing-main {
  padding: 40px 24px 64px;
}

.hero-section {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 0.9fr);
  gap: 28px;
  align-items: center;
  min-height: calc(100vh - 148px);
}

.eyebrow {
  display: inline-block;
  color: var(--text-tertiary);
  font-size: 12px;
  font-weight: 510;
  letter-spacing: 0;
  text-transform: uppercase;
}

.hero-copy h1,
.section-heading h2,
.closing-section h2 {
  color: var(--text-primary);
  font-weight: 510;
  line-height: 1.1;
  letter-spacing: 0;
}

.hero-copy h1 {
  margin-top: 16px;
  max-width: 720px;
  font-size: 52px;
  line-height: 1.12;
  letter-spacing: 0;
}

.hero-description,
.section-heading p,
.closing-section p {
  color: var(--text-tertiary);
  line-height: 1.75;
  font-size: 16px;
}

.hero-description {
  margin-top: 18px;
  max-width: 640px;
  font-size: 18px;
}

.hero-actions,
.hero-highlights {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-actions {
  margin-top: 28px;
}

.hero-primary {
  min-width: 126px;
}

.hero-highlights {
  margin-top: 22px;
}

.hero-pill,
.status-pill,
.timeline-step,
.card-index {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.025);
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 510;
}

.panel-card {
  border-radius: var(--radius-panel);
  border: 1px solid var(--border-default);
  background:
    linear-gradient(180deg, rgba(229, 238, 245, 0.035), rgba(229, 238, 245, 0.018)),
    rgba(15, 21, 24, 0.88);
  box-shadow:
    inset 0 1px 0 rgba(229, 238, 245, 0.04),
    0 20px 70px rgba(0, 0, 0, 0.28);
}

.hero-visual {
  padding: 22px;
}

.visual-header,
.visual-footnote,
.signal-card,
.traceability-diagram {
  display: flex;
}

.visual-header {
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.status-pill {
  color: var(--accent-hover);
  border-color: rgba(126, 209, 216, 0.24);
  background: rgba(88, 166, 173, 0.12);
}

.visual-stage {
  margin-top: 18px;
  display: grid;
  gap: 14px;
}

.signal-card {
  min-height: 128px;
  padding: 18px;
  border-radius: var(--radius-panel);
  border: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.02);
  flex-direction: column;
  justify-content: space-between;
}

.signal-card-primary {
  background:
    linear-gradient(135deg, rgba(88, 166, 173, 0.12), transparent 36%),
    rgba(229, 238, 245, 0.03);
}

.signal-card-accent {
  background:
    linear-gradient(135deg, rgba(198, 161, 91, 0.12), transparent 40%),
    rgba(229, 238, 245, 0.03);
}

.signal-card span,
.metric-card span {
  color: var(--text-quaternary);
  font-size: 12px;
  letter-spacing: 0;
  text-transform: uppercase;
}

.signal-card strong,
.metric-card strong {
  color: var(--text-primary);
  font-size: 22px;
  font-weight: 510;
  line-height: 1.2;
  letter-spacing: 0;
}

.signal-card p,
.capability-card p,
.timeline-card p,
.traceability-card p {
  color: var(--text-tertiary);
  line-height: 1.7;
  font-size: 14px;
}

.signal-card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.visual-footnote {
  align-items: center;
  gap: 10px;
  margin-top: 18px;
  color: var(--text-quaternary);
  font-size: 12px;
  letter-spacing: 0;
  text-transform: uppercase;
}

.visual-footnote span:nth-child(even) {
  width: 24px;
  height: 1px;
  background: rgba(255, 255, 255, 0.12);
}

.content-section {
  padding-top: 92px;
}

.section-heading {
  max-width: 760px;
}

.section-heading h2,
.closing-section h2 {
  margin-top: 14px;
  font-size: 34px;
  line-height: 1.12;
  letter-spacing: 0;
}

.section-heading p,
.closing-section p {
  margin-top: 16px;
}

.positioning-grid,
.capability-grid,
.timeline-grid {
  display: grid;
  gap: 18px;
  margin-top: 28px;
}

.positioning-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.metric-card,
.capability-card,
.timeline-card,
.traceability-card {
  padding: 24px;
}

.metric-card strong {
  margin-top: 10px;
  display: block;
}

.capability-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.capability-card h3,
.timeline-card h3,
.traceability-card h3 {
  margin-top: 18px;
  color: var(--text-primary);
  font-size: 22px;
  font-weight: 510;
  letter-spacing: 0;
}

.capability-card small {
  display: block;
  margin-top: 16px;
  color: var(--text-quaternary);
  line-height: 1.6;
}

.timeline-grid {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.timeline-step {
  color: var(--accent-hover);
  border-color: rgba(126, 209, 216, 0.22);
  background: rgba(88, 166, 173, 0.12);
}

.traceability-layout {
  display: grid;
  grid-template-columns: minmax(320px, 0.9fr) minmax(0, 1.1fr);
  gap: 18px;
  margin-top: 28px;
}

.traceability-diagram {
  min-height: 100%;
  padding: 24px;
  flex-direction: column;
  justify-content: center;
  gap: 18px;
}

.diagram-node {
  padding: 18px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.022);
}

.diagram-node span {
  color: var(--text-quaternary);
  font-size: 12px;
  letter-spacing: 0;
}

.diagram-node strong {
  display: block;
  margin-top: 10px;
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 510;
  letter-spacing: 0;
}

.diagram-link {
  width: 1px;
  height: 36px;
  margin-left: 17px;
  background: linear-gradient(180deg, rgba(198, 161, 91, 0.45), rgba(229, 238, 245, 0.08));
}

.traceability-points {
  display: grid;
  gap: 18px;
}

.closing-section {
  margin-top: 92px;
  padding: 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.closing-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 1100px) {
  .landing-header,
  .hero-section,
  .positioning-grid,
  .capability-grid,
  .timeline-grid,
  .traceability-layout,
  .closing-section {
    grid-template-columns: 1fr;
  }

  .landing-header {
    grid-template-columns: 1fr;
    justify-items: start;
  }

  .landing-nav {
    justify-content: flex-start;
  }

  .hero-section {
    min-height: auto;
    padding-top: 32px;
  }

  .timeline-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .closing-section {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .landing-main,
  .landing-header {
    padding-inline: 16px;
  }

  .landing-main {
    padding-top: 28px;
    padding-bottom: 40px;
  }

  .hero-copy h1 {
    font-size: 36px;
  }

  .panel-card,
  .metric-card,
  .capability-card,
  .timeline-card,
  .traceability-card,
  .traceability-diagram,
  .closing-section {
    border-radius: var(--radius-panel);
  }

  .hero-visual,
  .metric-card,
  .capability-card,
  .timeline-card,
  .traceability-card,
  .traceability-diagram,
  .closing-section {
    padding: 18px;
  }

  .signal-card-grid {
    grid-template-columns: 1fr;
  }

  .timeline-grid {
    grid-template-columns: 1fr;
  }
}
</style>
